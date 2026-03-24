package com.example.microquest.config;

import com.example.microquest.model.Category;
import com.example.microquest.model.Difficulty;
import com.example.microquest.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ViewModelAdviceTest {

    @Mock UserProfileService userProfileService;

    @InjectMocks ViewModelAdvice viewModelAdvice;

    @Test
    void categories_returnsAllCategoryValues() {
        assertThat(viewModelAdvice.categories())
                .containsExactly(Category.values());
    }

    @Test
    void difficulties_returnsAllDifficultyValues() {
        assertThat(viewModelAdvice.difficulties())
                .containsExactly(Difficulty.values());
    }

}
