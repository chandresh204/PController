package chad.orionsoft.pcontroller

import android.view.KeyEvent

class Dictionary {
    companion object {

        const val OS_LINUX = 0
        const val OS_WINDOWS =1
        const val PREFS_OS = "os"
    //    var os = OS_LINUX

        const val USBPROF = "usb_profile"

        //special keys
        const val ESC = "esc"
        const val PRINT = "print"
        const val BKSPACE = "bkspace"
        const val TAB = "tab"
        const val CAPS = "caps"
        const val ENTER = "enter"
        const val LSHIFT = "lshift"
        const val RSHIFT = "rshift"
        const val LCTRL = "lcntrl"
        const val START = "start"
        const val LALT = "lalt"
        const val SPACE = "space"
        const val RALT = "ralt"
        const val RCTRL = "lctl"
        const val INSERT = "insert"
        const val HOME = "home"
        const val PAGEUP = "pageup"
        const val DELETE = "delete"
        const val END = "end"
        const val PAGEDOWN = "pagedown"
        const val UP = "a_up"
        const val LEFT = "a_left"
        const val DOWN = "a_down"
        const val RIGHT = "a_right"
        const val MOUSE_L = "mouseL"
        const val MOUSE_M = "mouseM"
        const val MOUSE_R = "mouseR"
        const val MOUSE_UP = "mouseSclUP"
        const val MOUSE_DOWN = "mouseSclDown"
        const val MOUSE_MV_UP = "mouseMVup"
        const val MOUSE_MV_DOWN = "mouseMVdown"
        const val MOUSE_MV_RIGHT = "mouseMVright"
        const val MOUSE_MV_LEFT = "mouseMVleft"
        const val GRAVE = "`"
        const val MINUS = "-"
        const val EQUALS = "="
        const val LBRACKET = "["
        const val RBRACKET = "]"
        const val BKSLASH = "\\"
        const val SEMICOLON = ";"
        const val QOT = "'"
        const val COMMA = ","
        const val PERIOD = "."
        const val SLASH = "/"
        const val NOT_DEFINED = "-1"

        // for default usb keyboard profile
        fun convertUSBKeyboardValue(keyCode: Int, os:Int) : String {
            var pcKey = ""
            if (os == OS_LINUX) {
                pcKey = when (keyCode) {
                    KeyEvent.KEYCODE_ESCAPE -> "Escape"
                    KeyEvent.KEYCODE_F1 -> "67"
                    KeyEvent.KEYCODE_F2 -> "68"
                    KeyEvent.KEYCODE_F3 -> "69"
                    KeyEvent.KEYCODE_F4 -> "70"
                    KeyEvent.KEYCODE_F5 -> "71"
                    KeyEvent.KEYCODE_F6 -> "72"
                    KeyEvent.KEYCODE_F7 -> "73"
                    KeyEvent.KEYCODE_F8 -> "74"
                    KeyEvent.KEYCODE_F9 -> "75"
                    KeyEvent.KEYCODE_F10 -> "76"
                    KeyEvent.KEYCODE_F11 -> "95"
                    KeyEvent.KEYCODE_F12 -> "96"
                    // PRINT -> "107"
                    KeyEvent.KEYCODE_SCROLL_LOCK -> "78"
                    KeyEvent.KEYCODE_BREAK -> "127"
                    KeyEvent.KEYCODE_GRAVE -> "49"
                    KeyEvent.KEYCODE_1 -> "10"
                    KeyEvent.KEYCODE_2 -> "11"
                    KeyEvent.KEYCODE_3 -> "12"
                    KeyEvent.KEYCODE_4 -> "13"
                    KeyEvent.KEYCODE_5 -> "14"
                    KeyEvent.KEYCODE_6 -> "15"
                    KeyEvent.KEYCODE_7 -> "16"
                    KeyEvent.KEYCODE_8 -> "17"
                    KeyEvent.KEYCODE_9 -> "18"
                    KeyEvent.KEYCODE_0 -> "19"
                    KeyEvent.KEYCODE_MINUS -> "20"
                    KeyEvent.KEYCODE_EQUALS -> "21"
                    KeyEvent.KEYCODE_DEL -> "22"
                    KeyEvent.KEYCODE_TAB -> "23"
                    KeyEvent.KEYCODE_Q -> "24"
                    KeyEvent.KEYCODE_W -> "25"
                    KeyEvent.KEYCODE_E -> "26"
                    KeyEvent.KEYCODE_R -> "27"
                    KeyEvent.KEYCODE_T -> "28"
                    KeyEvent.KEYCODE_Y -> "29"
                    KeyEvent.KEYCODE_U -> "30"
                    KeyEvent.KEYCODE_I -> "31"
                    KeyEvent.KEYCODE_O -> "32"
                    KeyEvent.KEYCODE_P -> "33"
                    KeyEvent.KEYCODE_LEFT_BRACKET -> "34"
                    KeyEvent.KEYCODE_RIGHT_BRACKET -> "35"
                    KeyEvent.KEYCODE_BACKSLASH -> "51"
                    KeyEvent.KEYCODE_CAPS_LOCK -> "66"
                    KeyEvent.KEYCODE_A -> "38"
                    KeyEvent.KEYCODE_S -> "39"
                    KeyEvent.KEYCODE_D -> "40"
                    KeyEvent.KEYCODE_F -> "41"
                    KeyEvent.KEYCODE_G -> "42"
                    KeyEvent.KEYCODE_H -> "43"
                    KeyEvent.KEYCODE_J -> "44"
                    KeyEvent.KEYCODE_K -> "45"
                    KeyEvent.KEYCODE_L -> "46"
                    KeyEvent.KEYCODE_SEMICOLON -> "47"
                    KeyEvent.KEYCODE_APOSTROPHE -> "48"
                    KeyEvent.KEYCODE_ENTER -> "36"
                    KeyEvent.KEYCODE_SHIFT_LEFT -> "50"
                    KeyEvent.KEYCODE_Z -> "52"
                    KeyEvent.KEYCODE_X -> "53"
                    KeyEvent.KEYCODE_C -> "54"
                    KeyEvent.KEYCODE_V -> "55"
                    KeyEvent.KEYCODE_B -> "56"
                    KeyEvent.KEYCODE_N -> "57"
                    KeyEvent.KEYCODE_M -> "58"
                    KeyEvent.KEYCODE_COMMA -> "59"
                    KeyEvent.KEYCODE_PERIOD -> "60"
                    KeyEvent.KEYCODE_SLASH -> "61"
                    KeyEvent.KEYCODE_SHIFT_RIGHT -> "62"
                    KeyEvent.KEYCODE_CTRL_LEFT -> "37"
                    // windows ?
                    KeyEvent.KEYCODE_ALT_LEFT -> "64"
                    KeyEvent.KEYCODE_SPACE -> "65"
                    KeyEvent.KEYCODE_ALT_RIGHT -> "108"
                    KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> "135"  // ?
                    KeyEvent.KEYCODE_CTRL_RIGHT -> "105"
                    KeyEvent.KEYCODE_INSERT -> "118"
                    KeyEvent.KEYCODE_MOVE_HOME -> "110"
                    KeyEvent.KEYCODE_PAGE_UP -> "112"
                    KeyEvent.KEYCODE_FORWARD_DEL -> "119"
                    KeyEvent.KEYCODE_MOVE_END -> "115"
                    KeyEvent.KEYCODE_PAGE_DOWN -> "117"
                    KeyEvent.KEYCODE_DPAD_UP -> "111"
                    KeyEvent.KEYCODE_DPAD_LEFT -> "113"
                    KeyEvent.KEYCODE_DPAD_DOWN -> "116"
                    KeyEvent.KEYCODE_DPAD_RIGHT -> "114"
                    KeyEvent.KEYCODE_NUM_LOCK -> "77"
                    KeyEvent.KEYCODE_NUMPAD_DIVIDE -> "106"
                    KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> "63"
                    KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> "82"
                    KeyEvent.KEYCODE_NUMPAD_ADD -> "86"
                    KeyEvent.KEYCODE_NUMPAD_ENTER -> "104"
                    KeyEvent.KEYCODE_NUMPAD_DOT -> "91"
                    KeyEvent.KEYCODE_NUMPAD_0 -> "90"
                    KeyEvent.KEYCODE_NUMPAD_1 -> "87"
                    KeyEvent.KEYCODE_NUMPAD_2 -> "88"
                    KeyEvent.KEYCODE_NUMPAD_3 -> "89"
                    KeyEvent.KEYCODE_NUMPAD_4 -> "83"
                    KeyEvent.KEYCODE_NUMPAD_5 -> "84"
                    KeyEvent.KEYCODE_NUMPAD_6 -> "85"
                    KeyEvent.KEYCODE_NUMPAD_7 -> "79"
                    KeyEvent.KEYCODE_NUMPAD_8 -> "80"
                    KeyEvent.KEYCODE_NUMPAD_9 -> "81"

                    else -> keyCode.toString()
                }
            }
            if (os == OS_WINDOWS) {
                pcKey = when(keyCode) {
                    KeyEvent.KEYCODE_ESCAPE -> "27"
                    KeyEvent.KEYCODE_F1 -> "112"
                    KeyEvent.KEYCODE_F2 -> "113"
                    KeyEvent.KEYCODE_F3 -> "114"
                    KeyEvent.KEYCODE_F4 -> "115"
                    KeyEvent.KEYCODE_F5 -> "116"
                    KeyEvent.KEYCODE_F6 -> "117"
                    KeyEvent.KEYCODE_F7 -> "118"
                    KeyEvent.KEYCODE_F8 -> "119"
                    KeyEvent.KEYCODE_F9 -> "120"
                    KeyEvent.KEYCODE_F10 -> "121"
                    KeyEvent.KEYCODE_F11 -> "122"
                    KeyEvent.KEYCODE_F12 -> "123"
                    // PRINT -> "int:44"
                    KeyEvent.KEYCODE_SCROLL_LOCK -> "145"
                    KeyEvent.KEYCODE_BREAK -> "19"
                    KeyEvent.KEYCODE_GRAVE -> "192"
                    KeyEvent.KEYCODE_1 -> "49"
                    KeyEvent.KEYCODE_2 -> "50"
                    KeyEvent.KEYCODE_3 -> "51"
                    KeyEvent.KEYCODE_4 -> "52"
                    KeyEvent.KEYCODE_5 -> "53"
                    KeyEvent.KEYCODE_6 -> "54"
                    KeyEvent.KEYCODE_7 -> "55"
                    KeyEvent.KEYCODE_8 -> "56"
                    KeyEvent.KEYCODE_9 -> "57"
                    KeyEvent.KEYCODE_0 -> "48"
                    KeyEvent.KEYCODE_MINUS -> "189"
                    KeyEvent.KEYCODE_EQUALS -> "187"
                    KeyEvent.KEYCODE_DEL -> "8"
                    KeyEvent.KEYCODE_TAB -> "9"
                    KeyEvent.KEYCODE_Q -> getWindowsCharValue("q")
                    KeyEvent.KEYCODE_W -> getWindowsCharValue("w")
                    KeyEvent.KEYCODE_E -> getWindowsCharValue("e")
                    KeyEvent.KEYCODE_R -> getWindowsCharValue("r")
                    KeyEvent.KEYCODE_T -> getWindowsCharValue("t")
                    KeyEvent.KEYCODE_Y -> getWindowsCharValue("y")
                    KeyEvent.KEYCODE_U -> getWindowsCharValue("u")
                    KeyEvent.KEYCODE_I -> getWindowsCharValue("i")
                    KeyEvent.KEYCODE_O -> getWindowsCharValue("o")
                    KeyEvent.KEYCODE_P -> getWindowsCharValue("p")
                    KeyEvent.KEYCODE_LEFT_BRACKET -> "219"
                    KeyEvent.KEYCODE_RIGHT_BRACKET -> "221"
                    KeyEvent.KEYCODE_BACKSLASH -> "220"
                    KeyEvent.KEYCODE_CAPS_LOCK -> "20"
                    KeyEvent.KEYCODE_A -> getWindowsCharValue("a")
                    KeyEvent.KEYCODE_S -> getWindowsCharValue("s")
                    KeyEvent.KEYCODE_D -> getWindowsCharValue("d")
                    KeyEvent.KEYCODE_F -> getWindowsCharValue("f")
                    KeyEvent.KEYCODE_G -> getWindowsCharValue("g")
                    KeyEvent.KEYCODE_H -> getWindowsCharValue("h")
                    KeyEvent.KEYCODE_J -> getWindowsCharValue("j")
                    KeyEvent.KEYCODE_K -> getWindowsCharValue("k")
                    KeyEvent.KEYCODE_L -> getWindowsCharValue("l")
                    KeyEvent.KEYCODE_SEMICOLON -> "186"
                    KeyEvent.KEYCODE_APOSTROPHE -> "222"
                    KeyEvent.KEYCODE_ENTER -> "13"
                    KeyEvent.KEYCODE_SHIFT_LEFT -> "160"
                    KeyEvent.KEYCODE_Z -> getWindowsCharValue("z")
                    KeyEvent.KEYCODE_X -> getWindowsCharValue("x")
                    KeyEvent.KEYCODE_C -> getWindowsCharValue("c")
                    KeyEvent.KEYCODE_V -> getWindowsCharValue("v")
                    KeyEvent.KEYCODE_B -> getWindowsCharValue("b")
                    KeyEvent.KEYCODE_N -> getWindowsCharValue("n")
                    KeyEvent.KEYCODE_M -> getWindowsCharValue("m")
                    KeyEvent.KEYCODE_COMMA -> "188"
                    KeyEvent.KEYCODE_PERIOD -> "190"
                    KeyEvent.KEYCODE_SLASH -> "191"
                    KeyEvent.KEYCODE_SHIFT_RIGHT -> "161"
                    KeyEvent.KEYCODE_CTRL_LEFT -> "162"
                    // windows = "int:92"
                    KeyEvent.KEYCODE_ALT_LEFT -> "164"
                    KeyEvent.KEYCODE_SPACE -> "32"
                    KeyEvent.KEYCODE_ALT_RIGHT -> "165"
                    KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> "?"  // ?
                    KeyEvent.KEYCODE_CTRL_RIGHT -> "163"
                    KeyEvent.KEYCODE_INSERT -> "45"
                    KeyEvent.KEYCODE_MOVE_HOME -> "36"
                    KeyEvent.KEYCODE_PAGE_UP -> "33"
                    KeyEvent.KEYCODE_FORWARD_DEL -> "46"
                    KeyEvent.KEYCODE_MOVE_END -> "35"
                    KeyEvent.KEYCODE_PAGE_DOWN -> "34"
                    KeyEvent.KEYCODE_DPAD_UP -> "38"
                    KeyEvent.KEYCODE_DPAD_LEFT -> "37"
                    KeyEvent.KEYCODE_DPAD_DOWN -> "40"
                    KeyEvent.KEYCODE_DPAD_RIGHT -> "39"
                    KeyEvent.KEYCODE_NUM_LOCK -> "144"
                    KeyEvent.KEYCODE_NUMPAD_DIVIDE -> "111"
                    KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> "106"
                    KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> "109"
                    KeyEvent.KEYCODE_NUMPAD_ADD -> "107"
                    KeyEvent.KEYCODE_NUMPAD_ENTER -> "13"
                    KeyEvent.KEYCODE_NUMPAD_DOT -> "110"
                    KeyEvent.KEYCODE_NUMPAD_0 -> "96"
                    KeyEvent.KEYCODE_NUMPAD_1 -> "97"
                    KeyEvent.KEYCODE_NUMPAD_2 -> "98"
                    KeyEvent.KEYCODE_NUMPAD_3 -> "99"
                    KeyEvent.KEYCODE_NUMPAD_4 -> "100"
                    KeyEvent.KEYCODE_NUMPAD_5 -> "101"
                    KeyEvent.KEYCODE_NUMPAD_6 -> "102"
                    KeyEvent.KEYCODE_NUMPAD_7 -> "103"
                    KeyEvent.KEYCODE_NUMPAD_8 -> "104"
                    KeyEvent.KEYCODE_NUMPAD_9 -> "105"

                    else -> keyCode.toString()
                }
            }
            return pcKey
        }

        fun getValueFromKeyName(keyString: String, os :Int) :String {
            var value :String = keyString
            if (os == OS_LINUX) {

                value =  when(keyString) {

                    ESC -> "Escape"
                    PRINT -> "107"
                    BKSPACE -> "22"
                    TAB -> "23"
                    CAPS -> "66"
                    ENTER -> "36"
                    LSHIFT -> "50"
                    RSHIFT -> "62"
                    LCTRL -> "37"
                    START -> "133"
                    LALT -> "64"
                    SPACE -> "65"
                    RALT -> "108"
                    RCTRL -> "105"
                    INSERT -> "118"
                    HOME -> "110"
                    PAGEUP -> "112"
                    DELETE -> "119"
                    END -> "115"
                    PAGEDOWN -> "117"
                    UP -> "111"
                    LEFT -> "113"
                    DOWN -> "116"
                    RIGHT -> "114"
                    MOUSE_L -> "<M1>"
                    MOUSE_M -> "<M2>"
                    MOUSE_R -> "<M3>"
                    MOUSE_UP -> "<M4>"
                    MOUSE_DOWN -> "<M5>"
                    MOUSE_MV_UP -> "<MV1>"
                    MOUSE_MV_DOWN -> "<MV2>"
                    MOUSE_MV_RIGHT -> "<MV3>"
                    MOUSE_MV_LEFT -> "<MV4>"

                    QOT -> "48"

                    NOT_DEFINED -> "-1"
                    else -> keyString
                }
            }
            if (os == OS_WINDOWS) {
                value =  when(keyString) {

                    ESC -> "27"
                    PRINT -> "44"
                    BKSPACE -> "8"
                    TAB -> "9"
                    CAPS -> "20"
                    ENTER -> "13"
                    LSHIFT -> "160"
                    RSHIFT -> "161"
                    LCTRL -> "162"
                    START -> "92"
                    LALT -> "164"
                    SPACE -> "32"
                    RALT -> "165"
                    RCTRL -> "163"
                    INSERT -> "45"
                    HOME -> "36"
                    PAGEUP -> "33"
                    DELETE -> "46"
                    END -> "35"
                    PAGEDOWN -> "34"
                    UP -> "38"
                    LEFT -> "37"
                    DOWN -> "40"
                    RIGHT -> "39"

                    GRAVE -> "192"
                    MINUS -> "189"
                    EQUALS -> "187"
                    LBRACKET -> "219"
                    RBRACKET -> "221"
                    BKSLASH -> "220"
                    SEMICOLON -> "186"
                    QOT -> "222"
                    COMMA -> "188"
                    PERIOD -> "190"
                    SLASH -> "191"

                    MOUSE_L -> "<M1>"
                    MOUSE_M -> "<M2>"
                    MOUSE_R -> "<M3>"
                    MOUSE_UP -> "<M4>"
                    MOUSE_DOWN -> "<M5>"
                    MOUSE_MV_UP -> "<MV1>"
                    MOUSE_MV_DOWN -> "<MV2>"
                    MOUSE_MV_RIGHT -> "<MV3>"
                    MOUSE_MV_LEFT -> "<MV4>"

                    NOT_DEFINED -> "-1"
                    else -> getWindowsCharValue(keyString)
                }
            }
            return value
        }

        fun getKeyNameFromValue(keyValue:String, os:Int) : String {

            var key = keyValue
            if (os == OS_LINUX) {
                key = when(keyValue) {
                    "Escape" -> ESC
                    "107" -> PRINT
                    "22" -> BKSPACE
                    "23" -> TAB
                    "66" -> CAPS
                    "36" -> ENTER
                    "50" -> LSHIFT
                    "62" -> RSHIFT
                    "37" -> LCTRL
                    "133" -> START
                    "64" -> LALT
                    "65" -> SPACE
                    "108" -> RALT
                    "105" -> RCTRL
                    "118" -> INSERT
                    "110" -> HOME
                    "112" -> PAGEUP
                    "119" -> DELETE
                    "115" -> END
                    "117" -> PAGEDOWN
                    "111" -> UP
                    "113" -> LEFT
                    "116" -> DOWN
                    "114" -> RIGHT
                    "<M1>" -> MOUSE_L
                    "<M2>" -> MOUSE_M
                    "<M3>" -> MOUSE_R
                    "<M4>" -> MOUSE_UP
                    "<M5>" -> MOUSE_DOWN

                    "48" -> QOT
                    "<MV1>" -> MOUSE_MV_UP
                    "<MV2>" -> MOUSE_MV_DOWN
                    "<MV3>" -> MOUSE_MV_RIGHT
                    "<MV4>" -> MOUSE_MV_LEFT

                    "-1" -> NOT_DEFINED

                    else -> keyValue
                }
            }
            if (os == OS_WINDOWS) {
                val valueIntIndex = keyValue.indexOf(":")
                val valueInt = keyValue.substring(valueIntIndex + 1)
                key = when (valueInt) {
                    "27" -> ESC
                    "44" -> PRINT
                    "8" -> BKSPACE
                    "9" -> TAB
                    "20" -> CAPS
                    "13" -> ENTER
                    "160" -> LSHIFT
                    "161" -> RSHIFT
                    "162" -> LCTRL
                    "92" -> START
                    "164" -> LALT
                    "32" -> SPACE
                    "165" -> RALT
                    "163" -> RCTRL
                    "45" -> INSERT
                    "36" -> HOME
                    "33" -> PAGEUP
                    "46" -> DELETE
                    "35" -> END
                    "34" -> PAGEDOWN
                    "38" -> UP
                    "37" -> LEFT
                    "40" -> DOWN
                    "39" -> RIGHT

                    "192" -> GRAVE
                    "189" -> MINUS
                    "187" -> EQUALS
                    "219" -> LBRACKET
                    "221" -> RBRACKET
                    "220" -> BKSLASH
                    "186" -> SEMICOLON
                    "222" -> QOT
                    "188" -> COMMA
                    "190" -> PERIOD
                    "191" -> SLASH
                    "<M1>" -> MOUSE_L
                    "<M2>" -> MOUSE_M
                    "<M3>" -> MOUSE_R
                    "<M4>" -> MOUSE_UP
                    "<M5>" -> MOUSE_DOWN
                    "<MV1>" -> MOUSE_MV_UP
                    "<MV2>" -> MOUSE_MV_DOWN
                    "<MV3>" -> MOUSE_MV_RIGHT
                    "<MV4>" -> MOUSE_MV_LEFT
                    else -> getWindowsChar(valueInt)
                }
            }
            return key
        }

        private fun getWindowsCharValue(l: String) : String {
            return when (l) {
                "a" -> "65"
                "b" -> "66"
                "c" -> "67"
                "d" -> "68"
                "e" -> "69"
                "f" -> "70"
                "g" -> "71"
                "h" -> "72"
                "i" -> "73"
                "j" -> "74"
                "k" -> "75"
                "l" -> "76"
                "m" -> "77"
                "n" -> "78"
                "o" -> "79"
                "p" -> "80"
                "q" -> "81"
                "r" -> "82"
                "s" -> "83"
                "t" -> "84"
                "u" -> "85"
                "v" -> "86"
                "w" -> "87"
                "x" -> "88"
                "y" -> "89"
                "z" -> "90"
                "0" -> "48"
                "1" -> "49"
                "2" -> "50"
                "3" -> "51"
                "4" -> "52"
                "5" -> "53"
                "6" -> "54"
                "7" -> "55"
                "8" -> "56"
                "9" -> "57"
                else -> "0"
            }
        }

        private fun getWindowsChar(v : String) : String {
            return when(v) {
                "65" -> "a"
                "66" -> "b"
                "67" -> "c"
                "68" -> "d"
                "69" -> "e"
                "70" -> "f"
                "71" -> "g"
                "72" -> "h"
                "73" -> "i"
                "74" -> "j"
                "75" -> "k"
                "76" -> "l"
                "77" -> "m"
                "78" -> "n"
                "79" -> "o"
                "80" -> "p"
                "81" -> "q"
                "82" -> "r"
                "83" -> "s"
                "84" -> "t"
                "85" -> "u"
                "86" -> "v"
                "87" -> "w"
                "88" -> "x"
                "89" -> "y"
                "90" -> "z"
                "48" -> "0"
                "49" -> "1"
                "50" -> "2"
                "51" -> "3"
                "52" -> "4"
                "53" ->"5"
                "54" -> "6"
                "55" -> "7"
                "56" -> "8"
                "57" -> "9"
                else -> "null"
            }
        }
    }
}