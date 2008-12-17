/*
 * @(#)java_props_md.c	1.39 06/10/10
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

#include <windows.h>
#include <shlobj.h>
#include <objidl.h>
#include <locale.h>
#include <sys/types.h>
#include <sys/timeb.h>
#include <tchar.h>

#include "locale_str.h"
#include "java_props.h"

#ifndef VER_PLATFORM_WIN32_WINDOWS
#define VER_PLATFORM_WIN32_WINDOWS 1
#endif

#define SHELL_KEY "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders"

/* Encodings for Windows language groups. According to
   www.microsoft.com/globaldev/faqs/locales.asp,
   some locales do not have codepages, and are
   supported in Windows 2000/XP solely through Unicode.
   In this case, we use utf-8 encoding */

static char *encoding_names[] = {
    "Cp1250",    /*  0:Latin 2  */
    "Cp1251",    /*  1:Cyrillic */
    "Cp1252",    /*  2:Latin 1  */
    "Cp1253",    /*  3:Greek    */
    "Cp1254",    /*  4:Latin 5  */
    "Cp1255",    /*  5:Hebrew   */
    "Cp1256",    /*  6:Arabic   */
    "Cp1257",    /*  7:Baltic   */
    "Cp1258",    /*  8:Viet Nam */
    "MS874",     /*  9:Thai     */
    "MS932",     /* 10:Japanese */
    "GBK",       /* 11:PRC GBK  */
    "MS949",     /* 12:Korean Extended Wansung */
    "MS950",     /* 13:Chinese (Taiwan, Hongkong, Macau) */
    "utf-8",     /* 14:Unicode  */
    "MS1361",    /* 15:Korean Johab */
};

/*
 * List mapping from LanguageID to Java locale IDs.  
 * The entries in this list should not be construed to suggest we actually have
 * full locale-data and other support for all of these locales; these are
 * merely all of the Windows locales for which we could construct an accurate
 * locale ID.
 *
 * Some of the language IDs below are not yet used by Windows, but were
 * defined by Microsoft for other products, such as Office XP. They may
 * become Windows language IDs in the future.
 * 
 */
typedef struct LANGIDtoLocale {
    WORD    langID;
    WORD    encoding;
    char*   javaID;
} LANGIDtoLocale;

static LANGIDtoLocale langIDMap[] = {
    /* fallback locales to use when the country code doesn't match anything we have */
    0x01,    6, "ar", 
    0x02,    1, "bg", 
    0x03,    2, "ca", 
    0x04,   11, "zh", 
    0x05,    0, "cs", 
    0x06,    2, "da", 
    0x07,    2, "de", 
    0x08,    3, "el", 
    0x09,    2, "en", 
    0x0a,    2, "es", 
    0x0b,    2, "fi", 
    0x0c,    2, "fr", 
    0x0d,    5, "iw", 
    0x0e,    0, "hu", 
    0x0f,    2, "is", 
    0x10,    2, "it", 
    0x11,   10, "ja", 
    0x12,   12, "ko", 
    0x13,    2, "nl", 
    0x14,    2, "no", 
    0x15,    0, "pl", 
    0x16,    2, "pt", 
    0x17,    2, "rm", 
    0x18,    0, "ro", 
    0x19,    1, "ru", 
    0x1a,    0, "sh", 
    0x1b,    0, "sk", 
    0x1c,    0, "sq", 
    0x1d,    2, "sv", 
    0x1e,    9, "th", 
    0x1f,    4, "tr", 
    0x20,    2, "ur", 
    0x21,    2, "in", 
    0x22,    1, "uk", 
    0x23,    1, "be", 
    0x24,    0, "sl", 
    0x25,    7, "et", 
    0x26,    7, "lv", 
    0x27,    7, "lt", 
    0x29,    6, "fa", 
    0x2a,    8, "vi", 
    0x2b,   14, "hy",
    0x2c,    4, "az",
    0x2d,    2, "eu", 
    0x2f,    1, "mk", 
    0x31,    2, "ts",
    0x32,    2, "tn", 
    0x34,    2, "xh", 
    0x35,    2, "zu", 
    0x36,    2, "af", 
    0x37,   14, "ka",
    0x38,    2, "fo", 
    0x39,   14, "hi", 
    0x3a,    2, "mt", 
    0x3c,    2, "gd", 
    0x3d,    2, "yi",
    0x3e,    2, "ms", 
    0x3f,    1, "kk",
    0x40,    1, "ky",
    0x41,    2, "sw", 
    0x43,    1, "uz",
    0x44,    1, "tt",
    0x46,   14, "pa",
    0x47,   14, "gu",
    0x49,   14, "ta",
    0x4a,   14, "te",
    0x4b,   14, "kn",
    0x4e,   14, "mr",
    0x4f,   14, "sa",
    0x50,    1, "mn",
    0x56,    2, "gl",
    /* mappings for real Windows LCID values */ 
    0x0401,  6, "ar_SA", 
    0x0402,  1, "bg_BG", 
    0x0403,  2, "ca_ES", 
    0x0404, 13, "zh_TW", 
    0x0405,  0, "cs_CZ", 
    0x0406,  2, "da_DK", 
    0x0407,  2, "de_DE", 
    0x0408,  3, "el_GR", 
    0x0409,  2, "en_US", 
    0x040a,  2, "es_ES",  /* (traditional sort) */
    0x040b,  2, "fi_FI", 
    0x040c,  2, "fr_FR", 
    0x040d,  5, "iw_IL", 
    0x040e,  0, "hu_HU", 
    0x040f,  2, "is_IS", 
    0x0410,  2, "it_IT", 
    0x0411, 10, "ja_JP", 
    0x0412, 12, "ko_KR", 
    0x0413,  2, "nl_NL", 
    0x0414,  2, "no_NO", 
    0x0415,  0, "pl_PL", 
    0x0416,  2, "pt_BR", 
    0x0417,  2, "rm_CH",
    0x0418,  0, "ro_RO", 
    0x0419,  1, "ru_RU", 
    0x041a,  0, "hr_HR", 
    0x041b,  0, "sk_SK", 
    0x041c,  0, "sq_AL", 
    0x041d,  2, "sv_SE", 
    0x041e,  9, "th_TH", 
    0x041f,  4, "tr_TR", 
    0x0420,  6, "ur_PK",
    0x0421,  2, "in_ID", 
    0x0422,  1, "uk_UA", 
    0x0423,  1, "be_BY", 
    0x0424,  0, "sl_SI", 
    0x0425,  7, "et_EE", 
    0x0426,  7, "lv_LV", 
    0x0427,  7, "lt_LT", 
    0x0429,  6, "fa_IR", 
    0x042a,  8, "vi_VN", 
    0x042b, 14, "hy_AM",  /* Armenian  */
    0x042c,  4, "az_AZ",  /* Azeri_Latin */
    0x042d,  2, "eu_ES", 
/*  0x042e,  2, "??",      no ISO-639 abbreviation for Sorbian */
    0x042f,  1, "mk_MK", 
/*  0x0430,  2, "??",      no ISO-639 abbreviation for Sutu */
    0x0431,  2, "ts",     /* (country?) */
    0x0432,  2, "tn_BW",
/*  0x0433,  2, "??",      no ISO-639 abbreviation for Venda */  
    0x0434,  2, "xh",     /* (country?) */
    0x0435,  2, "zu",     /* (country?) */
    0x0436,  2, "af_ZA", 
    0x0437, 14, "ka_GE",  /* Georgian   */
    0x0438,  2, "fo_FO", 
    0x0439, 14, "hi_IN", 
    0x043a,  2, "mt_MT",
/*  0x043b,  2, "??",      no ISO-639 abbreviation for Sami */
    0x043c,  2, "gd_GB",
    0x043d,  2, "yi",     /* (country?) */
    0x043e,  2, "ms_MY", 
    0x043f,  1, "kk_KZ",  /* Kazakh */
    0x0440,  1, "ky_KG",  /* Kyrgyz     */
    0x0441,  2, "sw_KE", 
    0x0443,  1, "uz_UZ",  /* Uzbek_Cyrillic*/
    0x0444,  1, "tt",     /* Tatar, no ISO-3166 abbreviation */
    0x0446, 14, "pa_IN",  /* Punjabi   */
    0x0447, 14, "gu_IN",  /* Gujarati  */
    0x0449, 14, "ta_IN",  /* Tamil     */
    0x044a, 14, "te_IN",  /* Telugu    */
    0x044b, 14, "kn_IN",  /* Kannada   */
    0x044e, 14, "mr_IN",  /* Marathi   */
    0x044f, 14, "sa_IN",  /* Sanskrit  */
    0x0450,  1, "mn_MN",  /* Mongolian */
    0x0456,  2, "gl_ES",  /* Galician  */
/*  0x0457, 14, "??_IN",  /* Konkani, no ISO-639 abbreviation*/
/*  0x045a, 14, "??_SY",  /* Syriac, no ISO-639 abbreviation*/
/*  0x0465, 14, "??_MV",  /* Divehi, no ISO-639 abbreviation*/
    0x0801,  6, "ar_IQ", 
    0x0804, 11, "zh_CN", 
    0x0807,  2, "de_CH", 
    0x0809,  2, "en_GB", 
    0x080a,  2, "es_MX", 
    0x080c,  2, "fr_BE", 
    0x0810,  2, "it_CH", 
    0x0812, 15, "ko_KR",  /* Korean(Johab)*/
    0x0813,  2, "nl_BE", 
    0x0814,  2, "no_NO_NY",
    0x0816,  2, "pt_PT", 
    0x0818,  0, "ro_MD",
    0x0819,  1, "ru_MD",
    0x081a,  0, "sh_YU", 
    0x081d,  2, "sv_FI", 
    0x082c,  1, "az_AZ",  /* Azeri_Cyrillic */
    0x083c,  2, "ga_IE",
    0x083e,  2, "ms_BN", 
    0x0843,  4, "uz_UZ",  /* Uzbek_Latin  */
    0x0c01,  6, "ar_EG", 
    0x0c04, 13, "zh_HK", 
    0x0c07,  2, "de_AT", 
    0x0c09,  2, "en_AU", 
    0x0c0a,  2, "es_ES",  /* (modern sort) */ 
    0x0c0c,  2, "fr_CA", 
    0x0c1a,  1, "sr_YU", 
    0x1001,  6, "ar_LY", 
    0x1004, 11, "zh_SG", 
    0x1007,  2, "de_LU", 
    0x1009,  2, "en_CA", 
    0x100a,  2, "es_GT", 
    0x100c,  2, "fr_CH", 
    0x1401,  6, "ar_DZ", 
    0x1404, 13, "zh_MO", 
    0x1407,  2, "de_LI", 
    0x1409,  2, "en_NZ", 
    0x140a,  2, "es_CR", 
    0x140c,  2, "fr_LU", 
    0x1801,  6, "ar_MA", 
    0x1809,  2, "en_IE", 
    0x180a,  2, "es_PA", 
    0x180c,  2, "fr_MC", 
    0x1c01,  6, "ar_TN", 
    0x1c09,  2, "en_ZA", 
    0x1c0a,  2, "es_DO", 
    0x2001,  6, "ar_OM", 
    0x2009,  2, "en_JM", 
    0x200a,  2, "es_VE", 
    0x2401,  6, "ar_YE", 
    0x2409,  2, "en",     /* ("Caribbean", which could be any of many countries) */
    0x240a,  2, "es_CO", 
    0x2801,  6, "ar_SY", 
    0x2809,  2, "en_BZ", 
    0x280a,  2, "es_PE", 
    0x2c01,  6, "ar_JO", 
    0x2c09,  2, "en_TT", 
    0x2c0a,  2, "es_AR", 
    0x3001,  6, "ar_LB", 
    0x3009,  2, "en_ZW", 
    0x300a,  2, "es_EC", 
    0x3401,  6, "ar_KW", 
    0x3409,  2, "en_PH", 
    0x340a,  2, "es_CL", 
    0x3801,  6, "ar_AE", 
    0x380a,  2, "es_UY", 
    0x3c01,  6, "ar_BH", 
    0x3c0a,  2, "es_PY", 
    0x4001,  6, "ar_QA", 
    0x400a,  2, "es_BO", 
    0x440a,  2, "es_SV", 
    0x480a,  2, "es_HN", 
    0x4c0a,  2, "es_NI", 
    0x500a,  2, "es_PR"
}; 

/*
 * binary-search our list of LANGID values.  If we don't find the 
 * one we're looking for, mask out the country code and try again 
 * with just the primary language ID
 */
static int
getLocaleEntryIndex(LANGID langID)
{
    int index = -1;
    int tries = 0;
    do {
	int lo, hi, mid;
	lo = 0;
	hi = sizeof(langIDMap) / sizeof(LANGIDtoLocale);
	while (index == -1 && lo < hi) {
	    mid = (lo + hi) / 2;
	    if (langIDMap[mid].langID == langID) {
		index = mid;
	    } else if (langIDMap[mid].langID > langID) {
		hi = mid;
	    } else {
		lo = mid + 1;
	    }
	}
	langID = PRIMARYLANGID(langID);
	++tries;
    } while (index == -1 && tries < 2);

    return index;
}

static char *
getEncodingInternal(int index)
{
    char * ret = encoding_names[langIDMap[index].encoding];
		
    //Traditional Chinese Windows should use MS950_HKSCS as the 
    //default encoding, if HKSCS patch has been installed.
    // "old" MS950 0xfa41 -> u+e001
    // "new" MS950 0xfa41 -> u+92db
    if (strcmp(ret, "MS950") == 0) {
	TCHAR  mbChar[2] = {(char)0xfa, (char)0x41};
	WCHAR  unicodeChar;
	MultiByteToWideChar(CP_ACP, 0, mbChar, 2, &unicodeChar, 1);
	if (unicodeChar == 0x92db) {
	    ret = "MS950_HKSCS";
	}
    } else {
	//SimpChinese Windows should use GB18030 as the default 
	//encoding, if gb18030 patch has been installed (on windows 
	//2000/XP, (1)Codepage 54936 will be available 
	//(2)simsun18030.ttc will exist under system fonts dir )
	if (strcmp(ret, "GBK") == 0 && IsValidCodePage(54936)) {
	    char systemPath[MAX_PATH + 1];
	    char* gb18030Font = "\\FONTS\\SimSun18030.ttc";
	    FILE *f = NULL;
	    if (GetWindowsDirectory(systemPath, MAX_PATH + 1) != 0 && 
		strlen(systemPath) + strlen(gb18030Font) < MAX_PATH + 1) {
		strcat(systemPath, "\\FONTS\\SimSun18030.ttc");
		if ((f = fopen(systemPath, "r")) != NULL) {
		    fclose(f);
		    ret = "GB18030";
		}
	    }
	}
    }

    return ret;
}

// Exported entries for AWT
DllExport const char *
getEncodingFromLangID(LANGID langID)
{
    int index = getLocaleEntryIndex(langID);
    
    if (index != (-1)) {
	return getEncodingInternal(index);
    } else {
	return "Cp1252";
    }
}

DllExport const char *
getJavaIDFromLangID(LANGID langID)
{
    int index = getLocaleEntryIndex(langID);
    
    if (index != (-1)) {
	return langIDMap[index].javaID;
    } else {
	return NULL;
    }
}

/* 
 * Code to figure out the user's home directory using the registry
*/
static char *
getHomeFromRegistry()
{
    HKEY key;
    int rc;
    DWORD type;
    char *p;
    char path[MAX_PATH+1];
    int size = MAX_PATH+1;

    rc = RegOpenKeyEx(HKEY_CURRENT_USER, SHELL_KEY, 0, KEY_READ, &key);
    if (rc != ERROR_SUCCESS) {
	// Shell folder doesn't exist??!!
	return NULL;
    }

    path[0] = 0;
    rc = RegQueryValueEx(key, "Desktop", 0, &type, path, &size);
    if (rc != ERROR_SUCCESS || type != REG_SZ) {
        return NULL;
    }
    RegCloseKey(key);
    /* Get the parent of Desktop directory */
    p = strrchr(path, '\\');
    if (p == NULL) {
	return NULL;
    }
    *p = '\0';
    return strdup(path);
}

/*
 * Code to figure out the user's home directory using shell32.dll
 */
typedef HRESULT (WINAPI *GetSpecialFolderType)(HWND, int, LPITEMIDLIST *);
typedef BOOL (WINAPI *GetPathFromIDListType)(LPCITEMIDLIST, LPSTR);

char *
getHomeFromShell32()
{
    HMODULE lib = LoadLibrary("SHELL32.DLL");
    GetSpecialFolderType do_get_folder;
    GetPathFromIDListType do_get_path;
    HRESULT rc;
    LPITEMIDLIST item_list = 0;
    char *p;
    char path[MAX_PATH+1];
    int size = MAX_PATH+1;

    if (lib == 0) {
	// We can't load the library !!??
	return NULL;
    }

    do_get_folder = (GetSpecialFolderType)GetProcAddress(lib, "SHGetSpecialFolderLocation");
    do_get_path = (GetPathFromIDListType)GetProcAddress(lib, "SHGetPathFromIDListA");

    if (do_get_folder == 0 || do_get_path == 0) {
	// the library doesn't hold the right functions !!??
	return NULL;
    }

    rc = (*do_get_folder)(NULL, CSIDL_DESKTOPDIRECTORY, &item_list);
    if (!SUCCEEDED(rc)) {
	// we can't find the shell folder.
	return NULL;
    }

    path[0] = 0;
    (*do_get_path)(item_list, path);

    /* Get the parent of Desktop directory */
    p = strrchr(path, '\\');
    if (p) {
        *p = 0;
    }

    /*
     * We've been successful.  Note that we don't free the memory allocated
     * by ShGetSpecialFolderLocation.  We only ever come through here once,
     * and only if the registry lookup failed, so it's just not worth it. 
     *
     * We also don't unload the SHELL32 DLL.  We've paid the hit for loading
     * it and we may need it again later.
     */

    return strdup(path);
}
typedef BOOL (WINAPI *ProcPresentType)(DWORD);

java_props_t *
GetJavaProperties(JNIEnv* env)
{
    static java_props_t sprops = {0};
    char *v; /* tmp var */

    if (sprops.user_dir) {
        return &sprops;
    }

    /* AWT properties */
    sprops.awt_toolkit = "sun.awt.windows.WToolkit";

    /* tmp dir */
    {
        char tmpdir[MAX_PATH + 1];
        /* we might want to check that this succeed */
        GetTempPath(MAX_PATH + 1, tmpdir);
        sprops.tmp_dir = strdup(tmpdir);
    }

    /* Printing properties */
    sprops.printerJob = "sun.awt.windows.WPrinterJob";

    /* Preferences properties */
    sprops.util_prefs_PreferencesFactory =  
                                           "java.util.prefs.WindowsPreferencesFactory";

    /* Java2D properties */
    sprops.graphics_env = "sun.awt.Win32GraphicsEnvironment";

    {
        char buf[max(512, MAX_PATH+1)];
        char *path = getenv("JAVA_FONTS");
        if (path != 0) {
            if (sprintf(buf, "%s", path) != -1) {
                v = buf; /* sysNativePath(buf); Is this necessary? */
            } else {
                v = "";
            }
        } else {
            v = "";
        }
        sprops.font_dir = strdup(v);
    }

    /* OS properties */
    {
        char buf[100];
        OSVERSIONINFO ver;    
	ver.dwOSVersionInfoSize = sizeof(ver);
	GetVersionEx(&ver);
	
	/*
	 * From msdn page on OSVERSIONINFOEX, current as of this
	 * writing decoding of dwMajorVersion and dwMinorVersion.
	 *
	 *  Operating system		dwMajorVersion	dwMinorVersion
	 * ==================		==============	==============
	 *
	 * Windows 95			4		0
	 * Windows 98			4		10
	 * Windows ME			4		90
	 * Windows 3.51			3		51
	 * Windows NT 4.0		4		0
	 * Windows 2000			5		0
	 * Windows XP			5		1
	 * Windows Server 2003 family	5		2
	 *
	 * This mapping will presumably be augmented as new Windows
	 * versions are released.
	 */
	switch (ver.dwPlatformId) {
	case VER_PLATFORM_WIN32s:
	    sprops.os_name = "Windows 3.1";
	    break;
	case VER_PLATFORM_WIN32_WINDOWS:
           if (ver.dwMajorVersion == 4) {
                switch (ver.dwMinorVersion) {
                case 0:
                    sprops.os_name = "Windows 95";
                    break;
                case 10:
                    sprops.os_name = "Windows 98";
                    break;
                case 90:
                    sprops.os_name = "Windows Me";
                    break;
                default:
                    sprops.os_name = "Windows 9X (unknown)";
                }
            } else {
                sprops.os_name = "Windows 9X (unknown)";
            }
            break;
	case VER_PLATFORM_WIN32_NT:
            if (ver.dwMajorVersion <= 4) {
	        sprops.os_name = "Windows NT";
            } else if (ver.dwMajorVersion == 5) {
	        switch (ver.dwMinorVersion) {
		case 0:
		    sprops.os_name = "Windows 2000";
		    break;
	        case 1:
		    sprops.os_name = "Windows XP";
		    break;
	        case 2:
		    sprops.os_name = "Windows 2003";
		    break;
	        default:
		    sprops.os_name = "Windows NT (unknown)";
		    break;
	      }
            } else {
                sprops.os_name = "Windows NT (unknown)";
            }
	    break;
        default:
            sprops.os_name = "Windows (unknown)";
            break;
	}
	sprintf(buf, "%d.%d", ver.dwMajorVersion, ver.dwMinorVersion);
	sprops.os_version = strdup(buf);
#if _M_IA64
        sprops.os_arch = "ia64";
#elif _X86_
        sprops.os_arch = "x86";
#else
        sprops.os_arch = "unknown";
#endif

	sprops.patch_level = strdup(ver.szCSDVersion);
    }

    /* Endianness of platform */
    {
        unsigned int endianTest = 0xff000000; 
        if (((char*)(&endianTest))[0] != 0) {
            sprops.cpu_endian = "big";
        } else {
            sprops.cpu_endian = "little";
        }
    }

    /* ISA list */
    {
        int procType = 0;
        SYSTEM_INFO info;
        HMODULE lib;
        ProcPresentType proc_present;

        GetSystemInfo(&info);
        
        if (strcmp(sprops.os_name, "Windows NT") == 0) {
            boolean mmx = 0;
            char list[100];
            list[0] = 0;

            lib = LoadLibrary("KERNEL32");
            proc_present = (ProcPresentType)GetProcAddress(lib,
                                               "IsProcessorFeaturePresent");
            if (proc_present) {
                mmx = (*proc_present)(PF_MMX_INSTRUCTIONS_AVAILABLE);
                procType = info.wProcessorLevel;
                switch (procType) {
                    case 6:
                        if (mmx) {
                            strcat(list, "pentium_pro+mmx pentium_pro ");
                        } else {
                            strcat(list, "pentium_pro ");
                        }
                        /* FALL THROUGH */
                    case 5:
                        if (mmx) {
                            strcat(list, "pentium+mmx pentium ");
                        } else {
                            strcat(list, "pentium ");
                        }
                        /* FALL THROUGH */
                    case 4:
                        strcat(list, "i486 ");
                        /* FALL THROUGH */
                    case 3:
                        strcat(list, "i386");
                }
                sprops.cpu_isalist = strdup(list);
            }
	    FreeLibrary(lib);
        } else {
            procType = info.dwProcessorType;
            switch(procType) {
                case PROCESSOR_INTEL_386:
                    sprops.cpu_isalist = "i386";
                    break;
                case PROCESSOR_INTEL_486:
                    sprops.cpu_isalist = "i486 i386";
                    break;
                case PROCESSOR_INTEL_PENTIUM:
                    sprops.cpu_isalist = "pentium i486 i386";
                    break;
#ifdef _M_IA64
                case PROCESSOR_INTEL_IA64:
                    sprops.cpu_isalist = "ia64";
                    break;
#endif
            }
        }
    }

    /*
     * User name
     * We try to avoid calling GetUserName as it turns out to
     * be surprisingly expensive on NT.  It pulls in an extra
     * 100 K of footprint.
     */
    {
        char *uname = getenv("USERNAME");
        if (uname != NULL && strlen(uname) > 0) {
	    sprops.user_name = strdup(uname);
        } else {
            char buf[100];
            int buflen = sizeof(buf);
            sprops.user_name =
                GetUserName(buf, &buflen) ? strdup(buf) : "unknown";
	}
    }

    /*
     * Home directory/
     *
     * We first look under a standard registry key.  If that fails we
     * fall back on using a SHELL32.DLL API.  If that fails we use a
     * default value. 
     *
     * Note: To save space we want to avoid loading SHELL32.DLL
     * unless really necessary.  However if we do load it, we leave it
     * in memory, as it may be needed again later.
     *
     * The normal result is that for a given user name XXX:
     *     On multi-user NT, user.home gets set to c:\winnt\profiles\XXX.
     *     On multi-user Win95, user.home gets set to c:\windows\profiles\XXX.
     *     On single-user Win95, user.home gets set to c:\windows.
     */
    {
	char *homep = getHomeFromRegistry();
	if (homep == NULL) {
	    homep = getHomeFromShell32();
	    if (homep == NULL) {
                homep = "C:\\";
	    }
        }
        sprops.user_home = homep;
    }

    /*
     *  user.language
     *  user.country, user.variant (if user's environment specifies them)
     *  file.encoding
     *  file.encoding.pkg
     */
    {
        /*
         * query the system for the current system default locale
         * (which is a Windows LCID value), 
         */
        LANGID langID = LANGIDFROMLCID(GetThreadLocale()); 
        {
            int index = getLocaleEntryIndex(langID);

            /*
             * if we didn't find the LCID that the system returned to us,
	     * we don't have a Java locale ID that corresponds to it. 
	     * Fall back on en_US.
             */
            if (index == -1) {
                sprops.language = "en";
                sprops.country = "US";
                sprops.encoding = "Cp1252";
            } else {

                /* otherwise, look up the corresponding Java locale ID from
                 * the list of Java locale IDs and set up the system properties
                 * accordingly.
                 */
            
                char* lang;
                char* ctry;
                char* variant;

                lang = strdup(langIDMap[index].javaID);
                ctry = lang;

                while (*ctry != '_' && *ctry != 0)
                    ++ctry;

                if (*ctry == '_') {
                    *ctry++ = 0;
                }
                
                variant = ctry;
                while (*variant != '_' && *variant != 0)
                    ++variant;

                if (*variant == '_') {
                    *variant++ = 0;
                }
                
                sprops.language = lang;
                sprops.country = ctry;
                sprops.variant = variant;
                sprops.encoding = getEncodingInternal(index);
            }
        }
    }

    sprops.unicode_encoding = "UnicodeLittle";
    /* User TIMEZONE */
    {
	/*
	 * We defer setting up timezone until it's actually necessary.
	 * Refer to TimeZone.getDefault(). However, the system
	 * property is necessary to be able to be set by the command
	 * line interface -D. Here temporarily set a null string to
	 * timezone.
	 */
	sprops.timezone = "";
    }

    /* Current directory */
    {
        char buf[MAX_PATH];
        GetCurrentDirectory(sizeof(buf), buf);
        sprops.user_dir = strdup(buf);
    }

    sprops.file_separator = "\\";
    sprops.path_separator = ";";
    sprops.line_separator = "\r\n";

    return &sprops;
}
