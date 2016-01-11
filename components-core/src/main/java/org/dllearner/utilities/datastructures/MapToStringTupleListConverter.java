/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/28/11
 * Time: 6:37 AM
 *
 * Convert Maps of String to String Tuple List
 */
public class MapToStringTupleListConverter implements Converter<Map<String,String>,List<StringTuple>> {

    @Override
    public List<StringTuple> convert(Map<String,String> source) {
        List<StringTuple> result = new ArrayList<>();

        Set<String> keys = source.keySet();

        for (String key : keys) {
            String value = source.get(key);
            result.add(new StringTuple(key, value));
        }

        return result;
    }
}
