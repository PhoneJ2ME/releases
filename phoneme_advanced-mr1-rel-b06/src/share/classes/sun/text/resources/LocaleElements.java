/*
 * Portions Copyright 2000-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License version 2 for more details (a copy is included at
 * /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public
 * License version 2 along with this work; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package sun.text.resources;

import java.util.ListResourceBundle;

public class LocaleElements extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Languages", // language names
                new String[][] {
                    { "ab", "Abkhazian" },
                    { "aa", "Afar" },
                    { "af", "Afrikaans" },
                    { "sq", "Albanian" },
                    { "am", "Amharic" },
                    { "ar", "Arabic" },
                    { "hy", "Armenian" },
                    { "as", "Assamese" },
                    { "ay", "Aymara" },
                    { "az", "Azerbaijani" },
                    { "ba", "Bashkir" },
                    { "eu", "Basque" },
                    { "bn", "Bengali" },
                    { "dz", "Bhutani" },
                    { "bh", "Bihari" },
                    { "bi", "Bislama" },
                    { "br", "Breton" },
                    { "bg", "Bulgarian" },
                    { "my", "Burmese" },
                    { "be", "Byelorussian" },
                    { "km", "Cambodian" },
                    { "ca", "Catalan" },
                    { "zh", "Chinese" },
                    { "co", "Corsican" },
                    { "hr", "Croatian" },
                    { "cs", "Czech" },
                    { "da", "Danish" },
                    { "nl", "Dutch" },
                    { "en", "English" },
                    { "eo", "Esperanto" },
                    { "et", "Estonian" },
                    { "fo", "Faroese" },
                    { "fj", "Fiji" },
                    { "fi", "Finnish" },
                    { "fr", "French" },
                    { "fy", "Frisian" },
                    { "gl", "Galician" },
                    { "ka", "Georgian" },
                    { "de", "German" },
                    { "el", "Greek" },
                    { "kl", "Greenlandic" },
                    { "gn", "Guarani" },
                    { "gu", "Gujarati" },
                    { "ha", "Hausa" },
                    { "he", "Hebrew" },
                    { "iw", "Hebrew" },
                    { "hi", "Hindi" },
                    { "hu", "Hungarian" },
                    { "is", "Icelandic" },
                    { "id", "Indonesian" },
                    { "in", "Indonesian" },
                    { "ia", "Interlingua" },
                    { "ie", "Interlingue" },
                    { "iu", "Inuktitut" },
                    { "ik", "Inupiak" },
                    { "ga", "Irish" },
                    { "it", "Italian" },
                    { "ja", "Japanese" },
                    { "jw", "Javanese" },
                    { "kn", "Kannada" },
                    { "ks", "Kashmiri" },
                    { "kk", "Kazakh" },
                    { "rw", "Kinyarwanda" },
                    { "ky", "Kirghiz" },
                    { "rn", "Kirundi" },
                    { "ko", "Korean" },
                    { "ku", "Kurdish" },
                    { "lo", "Laothian" },
                    { "la", "Latin" },
                    { "lv", "Latvian (Lettish)" },
                    { "ln", "Lingala" },
                    { "lt", "Lithuanian" },
                    { "mk", "Macedonian" },
                    { "mg", "Malagasy" },
                    { "ms", "Malay" },
                    { "ml", "Malayalam" },
                    { "mt", "Maltese" },
                    { "mi", "Maori" },
                    { "mr", "Marathi" },
                    { "mo", "Moldavian" },
                    { "mn", "Mongolian" },
                    { "na", "Nauru" },
                    { "ne", "Nepali" },
                    { "no", "Norwegian" },
                    { "oc", "Occitan" },
                    { "or", "Oriya" },
                    { "om", "Oromo (Afan)" },
                    { "ps", "Pashto (Pushto)" },
                    { "fa", "Persian" },
                    { "pl", "Polish" },
                    { "pt", "Portuguese" },
                    { "pa", "Punjabi" },
                    { "qu", "Quechua" },
                    { "rm", "Rhaeto-Romance" },
                    { "ro", "Romanian" },
                    { "ru", "Russian" },
                    { "sm", "Samoan" },
                    { "sg", "Sangho" },
                    { "sa", "Sanskrit" },
                    { "gd", "Scots Gaelic" },
                    { "sr", "Serbian" },
                    { "sh", "Serbo-Croatian" },
                    { "st", "Sesotho" },
                    { "tn", "Setswana" },
                    { "sn", "Shona" },
                    { "sd", "Sindhi" },
                    { "si", "Sinhalese" },
                    { "ss", "Siswati" },
                    { "sk", "Slovak" },
                    { "sl", "Slovenian" },
                    { "so", "Somali" },
                    { "es", "Spanish" },
                    { "su", "Sundanese" },
                    { "sw", "Swahili" },
                    { "sv", "Swedish" },
                    { "tl", "Tagalog" },
                    { "tg", "Tajik" },
                    { "ta", "Tamil" },
                    { "tt", "Tatar" },
                    { "te", "Telugu" },
                    { "th", "Thai" },
                    { "bo", "Tibetan" },
                    { "ti", "Tigrinya" },
                    { "to", "Tonga" },
                    { "ts", "Tsonga" },
                    { "tr", "Turkish" },
                    { "tk", "Turkmen" },
                    { "tw", "Twi" },
                    { "ug", "Uighur" },
                    { "uk", "Ukrainian" },
                    { "ur", "Urdu" },
                    { "uz", "Uzbek" },
                    { "vi", "Vietnamese" },
                    { "vo", "Volapuk" },
                    { "cy", "Welsh" },
                    { "wo", "Wolof" },
                    { "xh", "Xhosa" },
                    { "ji", "Yiddish" },
                    { "yi", "Yiddish" },
                    { "yo", "Yoruba" },
                    { "za", "Zhuang" },
                    { "zu", "Zulu" }
                }
            },
            { "Countries", // country names
                new String[][] {
                    { "AF", "Afghanistan" },
                    { "AL", "Albania" },
                    { "DZ", "Algeria" },
                    { "AD", "Andorra" },
                    { "AO", "Angola" },
                    { "AI", "Anguilla" },
                    { "AR", "Argentina" },
                    { "AM", "Armenia" },
                    { "AW", "Aruba" },
                    { "AU", "Australia" },
                    { "AT", "Austria" },
                    { "AZ", "Azerbaijan" },
                    { "BS", "Bahamas" },
                    { "BH", "Bahrain" },
                    { "BD", "Bangladesh" },
                    { "BB", "Barbados" },
                    { "BY", "Belarus" },
                    { "BE", "Belgium" },
                    { "BZ", "Belize" },
                    { "BJ", "Benin" },
                    { "BM", "Bermuda" },
                    { "BT", "Bhutan" },
                    { "BO", "Bolivia" },
                    { "BA", "Bosnia and Herzegovina" },
                    { "BW", "Botswana" },
                    { "BR", "Brazil" },
                    { "BN", "Brunei" },
                    { "BG", "Bulgaria" },
                    { "BF", "Burkina Faso" },
                    { "BI", "Burundi" },
                    { "KH", "Cambodia" },
                    { "CM", "Cameroon" },
                    { "CA", "Canada" },
                    { "CV", "Cape Verde" },
                    { "CF", "Central African Republic" },
                    { "TD", "Chad" },
                    { "CL", "Chile" },
                    { "CN", "China" },
                    { "CO", "Colombia" },
                    { "KM", "Comoros" },
                    { "CG", "Congo" },
                    { "CR", "Costa Rica" },
                    // Ivory Coast is older usage; Cd'I is now in common use in English
                    { "CI", "C\u00F4te d'Ivoire" },
                    { "HR", "Croatia" },
                    { "CU", "Cuba" },
                    { "CY", "Cyprus" },
                    { "CZ", "Czech Republic" },
                    { "DK", "Denmark" },
                    { "DJ", "Djibouti" },
                    { "DM", "Dominica" },
                    { "DO", "Dominican Republic" },
                    { "TP", "East Timor" },
                    { "EC", "Ecuador" },
                    { "EG", "Egypt" },
                    { "SV", "El Salvador" },
                    { "GQ", "Equatorial Guinea" },
                    { "ER", "Eritrea" },
                    { "EE", "Estonia" },
                    { "ET", "Ethiopia" },
                    { "FJ", "Fiji" },
                    { "FI", "Finland" },
                    { "FR", "France" },
                    { "GF", "French Guiana" },
                    { "PF", "French Polynesia" },
                    { "TF", "French Southern Territories" },
                    { "GA", "Gabon" },
                    { "GM", "Gambia" },
                    { "GE", "Georgia" },
                    { "DE", "Germany" },
                    { "GH", "Ghana" },
                    { "GR", "Greece" },
                    { "GP", "Guadeloupe" },
                    { "GT", "Guatemala" },
                    { "GN", "Guinea" },
                    { "GW", "Guinea-Bissau" },
                    { "GY", "Guyana" },
                    { "HT", "Haiti" },
                    { "HN", "Honduras" },
                    { "HK", "Hong Kong" },
                    { "HU", "Hungary" },
                    { "IS", "Iceland" },
                    { "IN", "India" },
                    { "ID", "Indonesia" },
                    { "IR", "Iran" },
                    { "IQ", "Iraq" },
                    { "IE", "Ireland" },
                    { "IL", "Israel" },
                    { "IT", "Italy" },
                    { "JM", "Jamaica" },
                    { "JP", "Japan" },
                    { "JO", "Jordan" },
                    { "KZ", "Kazakhstan" },
                    { "KE", "Kenya" },
                    { "KI", "Kiribati" },
                    { "KP", "North Korea" },
                    { "KR", "South Korea" },
                    { "KW", "Kuwait" },
                    { "KG", "Kyrgyzstan" },
                    { "LA", "Laos" },
                    { "LV", "Latvia" },
                    { "LB", "Lebanon" },
                    { "LS", "Lesotho" },
                    { "LR", "Liberia" },
                    { "LY", "Libya" },
                    { "LI", "Liechtenstein" },
                    { "LT", "Lithuania" },
                    { "LU", "Luxembourg" },
                    { "MK", "Macedonia" },
                    { "MG", "Madagascar" },
                    { "MY", "Malaysia" },
                    { "ML", "Mali" },
                    { "MT", "Malta" },
                    { "MQ", "Martinique" },
                    { "MR", "Mauritania" },
                    { "MU", "Mauritius" },
                    { "YT", "Mayotte" },
                    { "MX", "Mexico" },
                    { "FM", "Micronesia" },
                    { "MD", "Moldova" },
                    { "MC", "Monaco" },
                    { "MN", "Mongolia" },
                    { "MS", "Montserrat" },
                    { "MA", "Morocco" },
                    { "MZ", "Mozambique" },
                    { "MM", "Myanmar" },
                    { "NA", "Namibia" },
                    { "NP", "Nepal" },
                    { "NL", "Netherlands" },
                    { "AN", "Netherlands Antilles" },
                    { "NC", "New Caledonia" },
                    { "NZ", "New Zealand" },
                    { "NI", "Nicaragua" },
                    { "NE", "Niger" },
                    { "NG", "Nigeria" },
                    { "NU", "Niue" },
                    { "NO", "Norway" },
                    { "OM", "Oman" },
                    { "PK", "Pakistan" },
                    { "PA", "Panama" },
                    { "PG", "Papua New Guinea" },
                    { "PY", "Paraguay" },
                    { "PE", "Peru" },
                    { "PH", "Philippines" },
                    { "PL", "Poland" },
                    { "PT", "Portugal" },
                    { "PR", "Puerto Rico" },
                    { "QA", "Qatar" },
                    { "RO", "Romania" },
                    { "RU", "Russia" },
                    { "RW", "Rwanda" },
                    { "SA", "Saudi Arabia" },
                    { "SN", "Senegal" },
                    { "SP", "Serbia" },
                    { "SC", "Seychelles" },
                    { "SL", "Sierra Leone" },
                    { "SG", "Singapore" },
                    { "SK", "Slovakia" },
                    { "SI", "Slovenia" },
                    { "SO", "Somalia" },
                    { "ZA", "South Africa" },
                    { "ES", "Spain" },
                    { "LK", "Sri Lanka" },
                    { "SD", "Sudan" },
                    { "SR", "Suriname" },
                    { "SZ", "Swaziland" },
                    { "SE", "Sweden" },
                    { "CH", "Switzerland" },
                    { "SY", "Syria" },
                    { "TW", "Taiwan" },
                    { "TJ", "Tajikistan" },
                    { "TZ", "Tanzania" },
                    { "TH", "Thailand" },
                    { "TG", "Togo" },
                    { "TK", "Tokelau" },
                    { "TO", "Tonga" },
                    { "TT", "Trinidad and Tobago" },
                    { "TN", "Tunisia" },
                    { "TR", "Turkey" },
                    { "TM", "Turkmenistan" },
                    { "UG", "Uganda" },
                    { "UA", "Ukraine" },
                    { "AE", "United Arab Emirates" },
                    { "GB", "United Kingdom" },
                    { "US", "United States" },
                    { "UY", "Uruguay" },
                    { "UZ", "Uzbekistan" },
                    { "VU", "Vanuatu" },
                    { "VA", "Vatican" },
                    { "VE", "Venezuela" },
                    { "VN", "Vietnam" }, // One word
                    { "VG", "British Virgin Islands" },
                    { "VI", "U.S. Virgin Islands" },
                    { "EH", "Western Sahara" },
                    { "YE", "Yemen" },
                    { "YU", "Yugoslavia" },
                    { "ZR", "Zaire" },
                    { "ZM", "Zambia" },
                    { "ZW", "Zimbabwe" }
                }
            },
            { "%%EURO", "Euro" }, // Euro variant display name
            { "%%B", "Bokm\u00e5l" }, // Norwegian variant display name
            { "%%NY", "Nynorsk" },  // Norwegian variant display name
            { "LocaleNamePatterns",
                /* Formats for the display name of a locale, for a list of
                 * items, and for composing two items in a list into one item.
                 * The list patterns are used in the variant name and in the
                 * full display name.
                 *
                 * This is the language-neutral form of this resource.
                 */
                new String[] {
                    "{0,choice,0#|1#{1}|2#{1} ({2})}", // Display name
                    "{0,choice,0#|1#{1}|2#{1},{2}|3#{1},{2},{3}}", // List
                    "{0},{1}" // List composition
                }
            },
            { "MonthNames",
                new String[] {
                    "January", // january
                    "February", // february
                    "March", // march
                    "April", // april
                    "May", // may
                    "June", // june
                    "July", // july
                    "August", // august
                    "September", // september
                    "October", // october
                    "November", // november
                    "December", // december
                    "" // month 13 if applicable
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "Jan", // abb january
                    "Feb", // abb february
                    "Mar", // abb march
                    "Apr", // abb april
                    "May", // abb may
                    "Jun", // abb june
                    "Jul", // abb july
                    "Aug", // abb august
                    "Sep", // abb september
                    "Oct", // abb october
                    "Nov", // abb november
                    "Dec", // abb december
                    "" // abb month 13 if applicable
                }
            },
            { "DayNames",
                new String[] {
                    "Sunday", // Sunday
                    "Monday", // Monday
                    "Tuesday", // Tuesday
                    "Wednesday", // Wednesday
                    "Thursday", // Thursday
                    "Friday", // Friday
                    "Saturday" // Saturday
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "Sun", // abb Sunday
                    "Mon", // abb Monday
                    "Tue", // abb Tuesday
                    "Wed", // abb Wednesday
                    "Thu", // abb Thursday
                    "Fri", // abb Friday
                    "Sat" // abb Saturday
                }
            },
            { "AmPmMarkers",
                new String[] {
                    "AM", // am marker
                    "PM" // pm marker
                }
            },
            { "Eras",
                new String[] { // era strings
                    "BC",
                    "AD"
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", // decimal pattern
                    "\u00a4 #,##0.00;-\u00a4 #,##0.00", // currency pattern
                    "#,##0%" // percent pattern
                }
            },
            { "NumberElements",
                new String[] {
                    ".", // decimal separator
                    ",", // group (thousands) separator
                    ";", // list separator
                    "%", // percent sign
                    "0", // native 0 digit
                    "#", // pattern digit
                    "-", // minus sign
                    "E", // exponential
                    "\u2030", // per mille
                    "\u221e", // infinity
                    "\ufffd" // NaN
                }
            },
            { "CurrencySymbols",
                new String[][] {
                   // localized versions should have entries of the form
                   // {<ISO 4217 currency code>, <localized currency symbol>}
                   // e.g., {"USD", "US$"}
                   // There are no entries for the root locale, so we fall
                   // back onto the ISO 4217 currency code.
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "h:mm:ss a z", // full time pattern
                    "h:mm:ss a z", // long time pattern
                    "h:mm:ss a", // medium time pattern
                    "h:mm a", // short time pattern
                    "EEEE, MMMM d, yyyy", // full date pattern
                    "MMMM d, yyyy", // long date pattern
                    "MMM d, yyyy", // medium date pattern
                    "M/d/yy", // short date pattern
                    "{1} {0}" // date-time pattern
                }
            },
            { "DateTimeElements",
                new String[] {
                    "1", // first day of week
                    "1" // min days in first week
                }
            },
            { "CollationElements", "" },
        };
    }
}
