/**
 *  This file is part of LEAP.
 * 
 *  LEAP was implemented as a plugin of DL-Learner http://dl-learner.org, 
 *  but some components can be used as stand-alone.
 * 
 *  LEAP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  LEAP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.dllearner.core.probabilistic.unife;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ParameterLearningException extends RuntimeException {

    public ParameterLearningException() {
        super();
    }

    public ParameterLearningException(String message) {
        super(message);
    }
    
    public ParameterLearningException(Throwable cause) {
        super(cause);
    }
}
