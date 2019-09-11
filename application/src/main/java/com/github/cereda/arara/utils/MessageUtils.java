/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.utils;

import com.github.cereda.arara.controller.ConfigurationController;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Implements utilitary methods for displaying messages.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class MessageUtils {
    
    // holds the default width for the
    // message body, in pixels
    private static final int WIDTH = 250;
    
    // let's start the UI manager and set
    // the default look and feel to be as
    // close as possible to the system
    static {
        
        // get the current look and feel
        String laf = (String) ConfigurationController.
                getInstance().get("ui.lookandfeel");
        
        // check if one is actually set
        if (!laf.equals("none")) {
        
            // use a special keyword to indicate
            // the use of a system look and feel
            if (laf.equals("system")) {
                laf = UIManager.getSystemLookAndFeelClassName();
            }
            
            // let's try it, in case it fails,
            // rely to the default look and feel
            try {

                // get the system look and feel name
                // and try to set it as default
                UIManager.setLookAndFeel(laf);
            }
            catch (Exception exception) {
                // quack, quack, quack
            }
            
        }
    }
    
    /**
     * Normalizes the icon type to one of the five available icons.
     * @param value An integer value.
     * @return The normalized integer value.
     */
    private static int normalizeIconType(int value) {
        
        // do the normalization according to the available
        // icons in the underlying message implementation
        switch (value) {
            case 1:
                value = JOptionPane.ERROR_MESSAGE;
                break;
            case 2:
                value = JOptionPane.INFORMATION_MESSAGE;
                break;
            case 3:
                value = JOptionPane.WARNING_MESSAGE;
                break;
            case 4:
                value = JOptionPane.QUESTION_MESSAGE;
                break;
            default:
                value = JOptionPane.PLAIN_MESSAGE;
                break;
        }
        return value;
    }
    
    /**
     * Normalizes the message width, so only valid nonzero values are accepted.
     * @param value An integer value corresponding to the message width.
     * @return The normalized width.
     */
    private static int normalizeMessageWidth(int value) {
        return (value > 0 ? value : WIDTH);
    }
    
    /**
     * Shows the message.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    public static void showMessage(int width, int type,
            String title, String text) {
        
        // effectively shows the message based
        // on the provided parameters
        JOptionPane.showMessageDialog(
                null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text
                ),
                title,
                normalizeIconType(type)
        );
    }
    
    /**
     * Shows the message. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    public static void showMessage(int type, String title, String text) {
        showMessage(WIDTH, type, title, text);
    }
    
    /**
     * Shows a message with options presented as an array of buttons.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    public static int showOptions(int width, int type, String title,
            String text, Object... buttons) {
        
        // returns the index of the selected button,
        // zero if nothing is selected
        return JOptionPane.showOptionDialog(
                null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text
                ),
                title,
                JOptionPane.DEFAULT_OPTION,
                normalizeIconType(type),
                null,
                buttons,
                buttons[0]
        ) + 1;
    }
    
    /**
     * Shows a message with options presented as an array of buttons. It relies
     * on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return  The index of the selected button, starting from 1.
     */
    public static int showOptions(int type, String title,
            String text, Object... buttons) {
        return showOptions(WIDTH, type, title, text, buttons);
    }
    
    /**
     * Shows a message with a text input.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int width, int type,
            String title, String text) {
        
        // get the string from the
        // input text, if any
        String input = JOptionPane.showInputDialog(
                null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text
                ),
                title,
                normalizeIconType(type)
        );
        
        // if the input is not null, that is,
        // the user actually typed something
        if (input != null) {
            
            // return the trimmed string
            return input.trim();
        }
        
        // nothing was typed, so let's
        // return an empty string
        return "";      
    }
    
    /**
     * Shows a message with a text input. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int type, String title, String text) {
        return showInput(WIDTH, type, title, text);
    }
    
    /**
     * Shows a message with options presented as a dropdown list of elements.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int width, int type, String title,
            String text, Object... elements) {
        
        // show the dropdown list and get
        // the selected object, if any
        Object index = JOptionPane.showInputDialog(
                null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text
                ),
                title,
                normalizeIconType(type),
                null,
                elements,
                elements[0]
        );
        
        // if it's not a null object, let's
        // find the corresponding index
        if (index != null) {
            
            // iterate through the array of elements
            for (int i = 0; i < elements.length; i++) {
                
                // if the element is found, simply
                // return the index plus 1, as zero
                // corresponds to no selection at all
                if (elements[i].equals(index)) {
                    return i + 1;
                }
                
            }
        }
        
        // nothing was selected,
        // simply return zero
        return 0;
    }
    
    /**
     * Shows a message with options presented as a dropdown list of elements. It
     * relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int type, String title,
            String text, Object... elements) {
        return showDropdown(WIDTH, type, title, text, elements);
    }
    
}
