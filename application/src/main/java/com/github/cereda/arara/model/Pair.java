/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2018, Paulo Roberto Massa Cereda 
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.model;

/**
 * Implements a pair of objects.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Pair<T, V> {

    // first element of the pair
    private final T firstElement;
    
    // second element of the pair
    private final V secondElement;

    /**
     * Constructor.
     * @param firstElement The first element.
     * @param secondElement The second element.
     */
    public Pair(T firstElement, V secondElement) {
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    /**
     * Gets the first element.
     * @return The first element.
     */
    public T getFirstElement() {
        return firstElement;
    }

    /**
     * Gets the second element.
     * @return The second element.
     */
    public V getSecondElement() {
        return secondElement;
    }
    
    /**
     * A shorter version for getting the first element.
     * @return The first element.
     */
    public T first() {
        return getFirstElement();
    }
    
    /**
     * A shorter version for getting the second element.
     * @return The second element.
     */
    public V second() {
        return getSecondElement();
    }
    
}
