/*
 * @(#)serial_port_export.c	1.21 06/04/05 @(#)
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 * 
 * This source file is specific for Qt-based configurations.
 */

/*=========================================================================
 * SYSTEM:    KVM
 * SUBSYSTEM: networking
 * FILE:      commProtocol_md.c
 * OVERVIEW:  Operations to support serial communication ports
 *            on Unix (native Unix support for the 'comm:' protocol)
 *=======================================================================*/

/*=======================================================================
 * Include files
 *=======================================================================*/

#include <sys/types.h>
#include <sys/stat.h>

/*
 * Note: To use this file on UNIX System V platforms like Solaris
 *       remove the ioctl.h include
 */
#include <sys/ioctl.h>

#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <termios.h>
#include <string.h>
#include <stdio.h>

#include <serial_port_impl.h>
#include <midpServices.h>

#include <midp_logging.h>
/*=======================================================================
 * Definitions and declarations
 *=======================================================================*/

static char* getLastErrorMsg(char* pHeader);

/* non system errors */
static char* COMM_BAD_PORTNUMBER = "Invalid port number";
static char* COMM_BAD_BAUDRATE   = "Invalid baud rate";

/*=========================================================================
 * Protocol implementation functions
 *=======================================================================*/

/*=========================================================================
 * FUNCTION:        freePortError
 * OVERVIEW:        
 * INTERFACE:
 *   parameters:    *pszError: Error message
 *   returns:       <nothing>
 *=======================================================================*/

void freePortError(char* pszError) {
}

/*=========================================================================
 * FUNCTION:        openPortByNumber
 * OVERVIEW:        Opens a serial port from the specified port number.
 * INTERFACE:
 *   parameters:    **ppszError: Error message
 *                  port:        Port number
 *                  baudRate:    The speed of the connection.
 *                  options:     Options to be used for the port.
 *   returns:       The open port 
 *=======================================================================*/

long openPortByNumber(char** ppszError, long port, long baudRate,
                      long options) {
    char* pFileName = NULL;

    switch (port) {
    case 0:
        /*
         * Note: to use this file on UNIX System V platforms like Solaris
         * change the line below to: pFileName = "/dev/term/a";
         */
        pFileName = "/dev/ttyS0";
        break;

    case 1:
        /*
         * Note: to use this file on UNIX System V platforms like Solaris
         * change the line below to: pFileName = "/dev/term/b";
         */
        pFileName = "/dev/ttyS1";
        break;

    default:
        *ppszError = COMM_BAD_PORTNUMBER;
        return -1;
    }

    return openPortByName(ppszError, pFileName, baudRate, options);
}

/*=========================================================================
 * FUNCTION:        openPortByName
 * OVERVIEW:        Opens a serial port from a specified name ie "/dev/term/a".
 * INTERFACE:
 *   parameters:    **ppszError:    Error message
 *                  *pszDeviceName: Port number
 *                  baudRate:       The speed of the connection.
 *                  options:        Options to be used for the port.
 *   returns:       The open port
 *=======================================================================*/

long openPortByName(char** ppszError, char* pszDeviceName, long baudRate,
                    long  options) {
    int hPort = 0;
    int openFlags = 0;

    /* do not become the controlling tty */
    openFlags = O_RDWR | O_NOCTTY;

    hPort = open(pszDeviceName, openFlags);
    if (hPort < 0) {
        *ppszError = getLastErrorMsg("open failed");
        return -1;
    }
    /* Set exclusive use flag to block other open calls with EBUSY. */
    ioctl(hPort, TIOCEXCL, 0);

    configurePort(ppszError, hPort, baudRate, options);
    if (*ppszError != NULL) {
        close(hPort);
        return -1;
    }

    return (long)hPort;
}

/*=========================================================================
 * FUNCTION:        configurePort
 * OVERVIEW:        Configures the open serial port.
 * INTERFACE:
 *   parameters:    **ppszError:   Error message
 *                  hPort:         The port to be configured.
 *                  baudRate:      The speed of the connection.
 *                  options:       Options to be used for the port.
 *   returns:       <nothing>
 *=======================================================================*/

void configurePort(char** ppszError, int hPort, long baudRate,
                          unsigned long options) {

    struct termios attributes;
    speed_t speed;
    int linesToSet;
    int flgs;

    memset(&attributes, 0, sizeof (attributes));
    attributes.c_cflag = CREAD | HUPCL | CLOCAL;

    switch(baudRate) {
    case 50:
        speed = B50;
        break;

    case 75:
        speed = B75;
        break;

    case 110:
        speed = B110;
        break;

    case 134:
        speed = B134;
        break;

    case 150:
        speed = B150;
        break;

    case 200:
        speed = B200;
        break;

    case 300:
        speed = B300;
        break;

    case 600:
        speed = B600;
        break;

    case 1200:
        speed = B1200;
        break;

    case 1800:
        speed = B1800;
        break;

    case 2400:
        speed = B2400;
        break;

    case 4800:
        speed = B4800;
        break;

    case 9600:
        speed = B9600;
        break;

    case 19200:
        speed = B19200;
        break;

    case 38400:
        speed = B38400;
        break;

    case 57600:
        speed = B57600;
        break;

    case 115200:
        speed = B115200;
        break;

    default:
        *ppszError = COMM_BAD_BAUDRATE;
        return;
    }

    cfsetispeed(&attributes, speed);
    cfsetospeed(&attributes, speed);

    /* default no parity */
    if (options & ODD_PARITY) {
        attributes.c_cflag |= PARENB | PARODD;
    } else if (options & EVEN_PARITY) {
        attributes.c_cflag |= PARENB;
    }

    /* CTS output flow control */
    if (options & AUTO_CTS) {
        attributes.c_cflag |= CRTSCTS;
    }

    /* RTS flow control */
    /*
     * This option is no support on Linux but if this file is used
     * on UNIX System V platform like Solaris then uncomment RTS option code. 
     *
     * if (options & AUTO_RTS) {
     *     attributes.c_cflag |= CRTSXOFF;
     * }
     */

    /* BITS_PER_CHAR_8 is 2 bits and includes BITS_PER_CHAR_7 */
    if ((options & BITS_PER_CHAR_8) == BITS_PER_CHAR_8) {
        attributes.c_cflag |= CS8;
    } else {
        attributes.c_cflag |= CS7;
    }

    /* default 1 stop bit */
    if (options & STOP_BITS_2) {
        attributes.c_cflag |= CSTOPB;
    }


    /* set non blocking since we are not using async methods */
    flgs = fcntl(hPort, F_GETFL, 0);
    fcntl(hPort, F_SETFL, flgs | O_NONBLOCK);

    /* no minimum amount of bytes, no secs */
    attributes.c_cc[VMIN] = 0;
    attributes.c_cc[VTIME] = 0;

    if (tcsetattr(hPort, TCSANOW, &attributes) == -1) {
        *ppszError = getLastErrorMsg("set attr failed");
        return;
    }

    /* Make sure the Data Terminal Ready line is on */
    linesToSet = TIOCM_DTR;
    ioctl(hPort, TIOCMBIS, &linesToSet);

    *ppszError = NULL;
}

/*=========================================================================
 * FUNCTION:        closePort
 * OVERVIEW:        Closes the open serial port.
 * INTERFACE:
 *   parameters:    hPort:  The open port.
 *   returns:       <nothing>
 *=======================================================================*/

void closePort(long hPort) {
    close((int)hPort);
}

/*=========================================================================
 * FUNCTION:        writeToPort
 * OVERVIEW:        Writes to the open port, without blocking
 * INTERFACE:
 *   parameters:    **ppszError:            Error message
 *                  hPort:                  The name of the port to write to.
 *                  *pBuffer:               The data to be written to the port.
 *                  nNumberOfBytesToWrite:  The number of bytes to write.
 *   returns:       The number of bytes written.
 *=======================================================================*/

long writeToPort(char** ppszError, long hPort, char* pBuffer,
                 long nNumberOfBytesToWrite) {
    int nNumberOfBytesWritten = 0;
    *ppszError = NULL;

    if (nNumberOfBytesToWrite == 0) {
        return 0;
    }

    nNumberOfBytesWritten = write((int)hPort, pBuffer, nNumberOfBytesToWrite);
    /* Possible improvement for Next Release:
             find out how to get signal when the comm port is ready.
             So this code can be used. Currently this code will block the
             Java thread since it does not register for QT signal.

             The Java code is written to allow this method
             write partial data and be recalled as many times
             as needed.

    if (nNumberOfBytesWritten == 0) {
            MidpReentryData* info;
            size_t infoSize;

            info = (MidpReentryData*)SNI_GetReentryData(&infoSize);
            if (info == NULL) {
                info = (MidpReentryData*)
                       SNI_AllocateReentryData(sizeof (MidpReentryData));
                info->waitingFor = COMM_SIGNAL;
                info->descriptor = hPort;
                info->status = 0;
                info->is_read = KNI_FALSE;
            }
            SNI_BlockThread();
            return 0;
     } else */ if (nNumberOfBytesWritten < 0) {
        if (errno == EAGAIN) {
            return 0;
        }

        *ppszError = getLastErrorMsg("write failed");
        return -1;
    }

    return nNumberOfBytesWritten;
}

/*=========================================================================
 * FUNCTION:        readFromPort
 * OVERVIEW:        reads from a serial port, without blocking
 * INTERFACE:
 *   parameters:    **ppszError:          Error message
 *                  hPort:                The name of the port to read from.
 *                  pBuffer:              The buffer to store the bytes read.
 *                  nNumberOfBytesToRead: The number of bytes to read.
 *   returns:       The number of bytes read.
 *=======================================================================*/

long readFromPort(char** ppszError, long hPort, char* pBuffer,
                 long nNumberOfBytesToRead) {
    int nNumberOfBytesRead = 0;
    *ppszError = NULL;

    if (nNumberOfBytesToRead == 0) {
        return 0;
    }

    nNumberOfBytesRead = read((int)hPort, pBuffer, nNumberOfBytesToRead);
    /* Possible improvement for Next Release:
             find out how to get signal when the comm port is ready.
             So this code can be used. Currently this code will block the
             Java thread since it does not register for QT signal.

             For now The Java code will poll like the RI does when
             the comm port is busy.

             Also this code is wrong because the CommConnection has a
             non-blocking option and this code does not check it.

    if (nNumberOfBytesRead == 0) {
            MidpReentryData* info;
            size_t infoSize;

            info = (MidpReentryData*)SNI_GetReentryData(&infoSize);
            if (info == NULL) {
                info = (MidpReentryData*)
                        SNI_AllocateReentryData(sizeof (MidpReentryData));
                info->waitingFor = COMM_WRITE_SIGNAL;
                info->descriptor = hPort;
                info->status = 0;
            }
            SNI_BlockThread();
            return 0;
    } else */ if (nNumberOfBytesRead < 0) {
        if (errno == EAGAIN) {
            return 0;
        }

        *ppszError = getLastErrorMsg("read failed");
        return -1;
    }

    return nNumberOfBytesRead;
}

/*=========================================================================
 * FUNCTION:        getLastErrorMsg
 * OVERVIEW:        Returns the last error message.
 * INTERFACE:
 *   parameters:    *pHeader:  The message header.
 *   returns:       The error message.
 *=======================================================================*/

static char* getLastErrorMsg(char* pHeader) {
    int lastError;
    char* pszError;

    lastError = errno;
    pszError = strerror(lastError);

    REPORT_INFO3(LC_PROTOCOL, "%s(%08.8X): %s", pHeader, lastError
		 pszError);

    return pszError;
}


