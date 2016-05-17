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
package org.dllearner.utils.unife;

import java.lang.reflect.Field;

/**
 * Utility class that uses reflection to get values of private fields and
 * methods during the test phase.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ReflectionHelper {

    public static <T, E> T getPrivateField(E instance, Class<T> returnType, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field privateField = instance.getClass().getDeclaredField(fieldName);
        privateField.setAccessible(true);
        T value = (T) privateField.get((E) instance);
        return value;
    }

    public static <T, E> T getPrivateField(E instance, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field privateField = instance.getClass().getDeclaredField(fieldName);
        privateField.setAccessible(true);
        T value = (T) privateField.get((E) instance);
        return value;
    }
    
    public static <T, E> void setPrivateField(E instance, String fieldName, T value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field privateField = instance.getClass().getDeclaredField(fieldName);
        privateField.setAccessible(true);
        privateField.set(instance, value);
    }

    public static <T, E> T getPrivateStaticField(Class<E> clazz, Class<T> returnType, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field privateField = clazz.getDeclaredField(fieldName);
        privateField.setAccessible(true);
        T value = (T) privateField.get(null);
        return value;
    }

    public static <T, E> T getPrivateStaticField(Class<E> clazz, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field privateField = clazz.getDeclaredField(fieldName);
        privateField.setAccessible(true);
        T value = (T) privateField.get(null);
        return value;
    }

}
