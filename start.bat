@echo off
:: start.bat — convenience wrapper so you can double-click or type "start" in cmd
:: Runs start.ps1 via PowerShell

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
