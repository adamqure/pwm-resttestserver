/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2018 The PWM Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package password.pwm.http;

import password.pwm.PwmApplication;
import password.pwm.PwmConstants;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmError;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.java.TimeDuration;
import password.pwm.util.logging.PwmLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PwmSessionWrapper
{
    private static final PwmLogger LOGGER = PwmLogger.forClass( PwmSessionWrapper.class );

    private transient PwmSession pwmSession;

    private PwmSessionWrapper( )
    {

    }

    public static void sessionMerge(
            final PwmApplication pwmApplication,
            final PwmSession pwmSession,
            final HttpSession httpSession
    )
            throws PwmUnrecoverableException
    {
        httpSession.setAttribute( PwmConstants.SESSION_ATTR_PWM_SESSION, pwmSession );

        setHttpSessionIdleTimeout( pwmApplication, pwmSession, httpSession );
    }


    public static PwmSession readPwmSession( final HttpSession httpSession )
            throws PwmUnrecoverableException
    {
        final PwmSession returnSession = ( PwmSession ) httpSession.getAttribute( PwmConstants.SESSION_ATTR_PWM_SESSION );
        if ( returnSession == null )
        {
            throw new PwmUnrecoverableException( new ErrorInformation( PwmError.ERROR_INTERNAL, "attempt to read PwmSession from HttpSession failed" ) );
        }
        return returnSession;
    }

    public static PwmSession readPwmSession(
            final HttpServletRequest httpRequest
    )
            throws PwmUnrecoverableException
    {
        return readPwmSession( httpRequest.getSession() );
    }

    public static void setHttpSessionIdleTimeout(
            final PwmApplication pwmApplication,
            final PwmSession pwmSession,
            final HttpSession httpSession
    )
            throws PwmUnrecoverableException
    {
        final IdleTimeoutCalculator.MaxIdleTimeoutResult result = IdleTimeoutCalculator.figureMaxSessionTimeout( pwmApplication, pwmSession );
        if ( httpSession.getMaxInactiveInterval() != result.getIdleTimeout().as( TimeDuration.Unit.SECONDS ) )
        {
            httpSession.setMaxInactiveInterval( ( int ) result.getIdleTimeout().as( TimeDuration.Unit.SECONDS ) );
            LOGGER.trace( pwmSession, "setting java servlet session timeout to " + result.getIdleTimeout().asCompactString()
                    + " due to " + result.getReason() );
        }
    }
}
