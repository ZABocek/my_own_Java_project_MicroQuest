package com.example.microquest.controller;

import com.example.microquest.model.UserProfile;
import com.example.microquest.service.SubmissionService;
import com.example.microquest.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.microquest.model.QuestSubmission;
import com.example.microquest.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/quests/{questId}")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserProfileService userProfileService;
    private final FileStorageService fileStorageService;

    public SubmissionController(SubmissionService submissionService,
                                UserProfileService userProfileService,
                                FileStorageService fileStorageService) {
        this.submissionService = submissionService;
        this.userProfileService = userProfileService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/submissions/{submissionId}/gif")
    @ResponseBody
    public ResponseEntity<Resource> serveGif(@PathVariable Long questId,
                                             @PathVariable Long submissionId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        QuestSubmission sub = submissionService.getSubmissionSecure(submissionId, user.getId(), isAdmin);
        Resource resource = new FileSystemResource(fileStorageService.resolveGif(sub.getGifPath()));
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }

    @GetMapping("/submit")
    public String showSubmitForm(@PathVariable Long questId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
        model.addAttribute("questId", questId);
        model.addAttribute("alreadySubmitted", submissionService.hasSubmitted(user.getId(), questId));
        return "quests/submit";
    }

    @PostMapping("/submit")
    public String handleSubmit(@PathVariable Long questId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("gif") List<MultipartFile> gifs,
                               @RequestParam(value = "caption", required = false) List<String> captions,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        List<MultipartFile> validGifs = gifs.stream()
                .filter(g -> g != null && !g.isEmpty())
                .toList();

        if (validGifs.isEmpty()) {
            UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
            model.addAttribute("questId", questId);
            model.addAttribute("alreadySubmitted", submissionService.hasSubmitted(user.getId(), questId));
            model.addAttribute("gifError", "Please choose at least one GIF file.");
            return "quests/submit";
        }

        try {
            UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
            for (int i = 0; i < validGifs.size(); i++) {
                String caption = (captions != null && i < captions.size()) ? captions.get(i) : "";
                submissionService.submitGif(questId, user, validGifs.get(i), caption);
            }
            int count = validGifs.size();
            redirectAttributes.addFlashAttribute("successMessage",
                    count == 1
                            ? "GIF uploaded! Your entry has been added to the quest page."
                            : count + " GIFs uploaded successfully!");
            return "redirect:/quests/" + questId;
        } catch (Exception e) {
            UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
            model.addAttribute("questId", questId);
            model.addAttribute("alreadySubmitted", submissionService.hasSubmitted(user.getId(), questId));
            model.addAttribute("errorMessage", e.getMessage());
            return "quests/submit";
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        // Extract questId from the request URI: /quests/{questId}/submit
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        String questId = "";
        for (int i = 0; i < parts.length; i++) {
            if ("quests".equals(parts[i]) && i + 1 < parts.length) {
                questId = parts[i + 1];
                break;
            }
        }
        redirectAttributes.addFlashAttribute("gifError",
                "File too large. Each GIF must be under 20 MB.");
        return "redirect:/quests/" + questId + "/submit";
    }

    @PostMapping("/submissions/{submissionId}/delete")
    public String deleteSubmission(@PathVariable Long questId,
                                   @PathVariable Long submissionId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        UserProfile user = userProfileService.getByUsername(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        submissionService.deleteSubmission(submissionId, user.getId(), isAdmin);
        redirectAttributes.addFlashAttribute("successMessage", "GIF deleted successfully.");
        return "redirect:/quests/" + questId;
    }
}
