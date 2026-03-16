package com.danceboard.shared

import kotlinx.serialization.Serializable

@Serializable
enum class DanceType {
    STANDARD,  // Walc, Tango, Walc Wiedeński, Foxtrot, Quickstep
    LATIN,     // Samba, Cha-cha, Rumba, Paso Doble, Jive
    SOCIAL // modern-jive //hustle

}