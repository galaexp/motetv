package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// iOS Glassmorphic Light Design System Tokens
// ==========================================

// Canvas & Ambient Atmospheres
val IosCanvasBackground = Color(0xFFF3F5F9)
val IosCanvasAmbientSky = Color(0xFFE6EEF8)
val IosCanvasAmbientLilac = Color(0xFFEDE8F5)
val IosCanvasAmbientPure = Color(0xFFFFFFFF)

// Frosted Glass Layer Materials
val IosGlassFill = Color(0xCCFFFFFF)           // 80% Frosted Glass
val IosGlassElevated = Color(0xE6FFFFFF)       // 90% Glass for Modals & Nav
val IosGlassUltra = Color(0xF2FFFFFF)          // 95% Glass for Top Bars
val IosGlassSubtle = Color(0x99FFFFFF)         // 60% Glass for secondary chips
val IosGlassCard = Color(0xD9FFFFFF)           // 85% Frosted Glass Cards

// Specular Highlights & Outlines
val IosGlassBorderHighlight = Color(0x99FFFFFF) // 60% Pure White Specular
val IosGlassBorderSubtle = Color(0x18000000)    // 10% Soft Charcoal Outline
val IosGlassDivider = Color(0x12000000)         // 7% Hairline Divider
val IosGlassShadow = Color(0x0D000000)          // Soft Diffuse Drop Shadow

// iOS SF High-Contrast Typography
val IosTextPrimary = Color(0xFF1C1C1E)         // Apple Primary Label
val IosTextSecondary = Color(0xFF636366)       // Apple Secondary Label
val IosTextTertiary = Color(0xFF8E8E93)        // Apple Tertiary / Hint
val IosTextMuted = Color(0xFFA0A5B0)           // Footnotes & Sub-captions

// iOS System Vibrancies
val IosSystemBlue = Color(0xFF007AFF)          // Iconic Apple System Blue
val IosSystemBlueSubtle = Color(0x1F007AFF)    // Blue Glass Tint
val IosSystemRed = Color(0xFFFF3B30)           // Apple System Red / Power
val IosSystemRedSubtle = Color(0x1FFF3B30)     // Red Glass Tint
val IosSystemGreen = Color(0xFF34C759)         // Apple System Green / Connected
val IosSystemGreenSubtle = Color(0x1F34C759)   // Green Glass Tint
val IosSystemOrange = Color(0xFFFF9500)        // Apple System Orange
val IosSystemPurple = Color(0xFFAF52DE)        // Apple System Purple
val IosSystemIndigo = Color(0xFF5856D6)        // Apple System Indigo
val IosSystemTeal = Color(0xFF30B0C7)          // Apple System Teal
val IosSystemGray = Color(0xFF8E8E93)          // Apple System Gray
val IosSystemGray5 = Color(0xFFE5E5EA)         // Soft Inset Pill
val IosSystemGray6 = Color(0xFFF2F2F7)         // Card Inset Background

// Backwards-compatible aliases dynamically mapped to iOS Glassmorphic Light:
val NovaObsidian = IosCanvasBackground
val NovaSurface = IosGlassFill
val NovaSurfaceElevated = IosGlassElevated
val NovaSurfaceHighlight = IosGlassCard
val NovaBorder = IosGlassBorderHighlight
val NovaBorderSubtle = IosGlassBorderSubtle

val NovaTextPrimary = IosTextPrimary
val NovaTextSecondary = IosTextSecondary
val NovaTextMuted = IosTextTertiary

val NovaAccent = IosSystemBlue
val NovaAccentSubtle = IosSystemBlueSubtle
val NovaSuccess = IosSystemGreen
val NovaWarning = IosSystemOrange
val NovaPower = IosSystemRed
