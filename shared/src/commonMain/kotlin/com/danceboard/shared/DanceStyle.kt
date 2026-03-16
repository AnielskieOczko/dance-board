package com.danceboard.shared

import kotlinx.serialization.Serializable

@Serializable
enum class DanceStyle {
    // Standard (5)
    WALTZ,
    TANGO,
    VIENNESE_WALTZ,
    SLOW_FOXTROT,
    QUICKSTEP,

    // Latin (5)
    SAMBA,
    CHA_CHA_CHA,
    RUMBA,
    PASO_DOBLE,
    JIVE,

    // Social
    MODERN_JIVE,
    // Catch-all
    OTHER
}