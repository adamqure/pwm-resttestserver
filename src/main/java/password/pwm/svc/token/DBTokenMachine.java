/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2017 The PWM Project
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

package password.pwm.svc.token;

import password.pwm.bean.SessionLabel;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.db.DatabaseAccessor;
import password.pwm.util.db.DatabaseTable;

import java.util.Iterator;

class DBTokenMachine implements TokenMachine {
    private DatabaseAccessor databaseAccessor;
    private TokenService tokenService;

    DBTokenMachine(final TokenService tokenService, final DatabaseAccessor databaseAccessor) {
        this.tokenService = tokenService;
        this.databaseAccessor = databaseAccessor;
    }

    public String generateToken(
            final SessionLabel sessionLabel,
            final TokenPayload tokenPayload
    )
            throws PwmUnrecoverableException, PwmOperationalException
    {
        return tokenService.makeUniqueTokenForMachine(sessionLabel, this);
    }

    public TokenPayload retrieveToken(final String tokenKey, final SessionLabel sessionLabel)
            throws PwmOperationalException, PwmUnrecoverableException
    {
        final String md5sumToken = tokenService.makeTokenHash(tokenKey);
        final String storedRawValue = databaseAccessor.get(DatabaseTable.TOKENS,md5sumToken);

        if (storedRawValue != null && storedRawValue.length() > 0 ) {
            return tokenService.fromEncryptedString(storedRawValue);
        }

        return null;
    }

    public void storeToken(final String tokenKey, final TokenPayload tokenPayload) throws PwmOperationalException, PwmUnrecoverableException {
        final String rawValue = tokenService.toEncryptedString(tokenPayload);
        final String md5sumToken = tokenService.makeTokenHash(tokenKey);
        databaseAccessor.put(DatabaseTable.TOKENS, md5sumToken, rawValue);
    }

    public void removeToken(final String tokenKey, final SessionLabel sessionLabel) throws PwmOperationalException, PwmUnrecoverableException {
        final String md5sumToken = tokenService.makeTokenHash(tokenKey);
        databaseAccessor.remove(DatabaseTable.TOKENS,tokenKey);
        databaseAccessor.remove(DatabaseTable.TOKENS,md5sumToken);
    }

    public int size() throws PwmOperationalException, PwmUnrecoverableException {
        return databaseAccessor.size(DatabaseTable.TOKENS);
    }

    public Iterator keyIterator() throws PwmOperationalException, PwmUnrecoverableException {
        return databaseAccessor.iterator(DatabaseTable.TOKENS);
    }

    public void cleanup() throws PwmUnrecoverableException, PwmOperationalException {
        tokenService.purgeOutdatedTokens();
    }

    public boolean supportsName() {
        return true;
    }
}
