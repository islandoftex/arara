/**
 * Rule checker, a tool for Arara
 * Copyright (c) 2015, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.rulechecker;

/**
 * Provides an implementation for a pair structure.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @param <A> Type of first element.
 * @param <B> Type of second element.
 * @since 1.0
 */
public class Pair<A, B> {
    
    // first element
    private A first;
    
    // second element
    private B second;

    /**
     * Gets the first element.
     * @return The first element.
     */
    public A getFirst() {
        return first;
    }

    /**
     * Sets the first element.
     * @param first The first element.
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * Gets the second element.
     * @return The second element.
     */
    public B getSecond() {
        return second;
    }

    /**
     * Sets the second element.
     * @param second The second element.
     */
    public void setSecond(B second) {
        this.second = second;
    }

}
