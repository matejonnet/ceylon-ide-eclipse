package com.redhat.ceylon.eclipse.code.editor;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FoldingMessages {
    private static final String BUNDLE_NAME= "com.redhat.ceylon.eclipse.code.editor.FoldingMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

    private FoldingMessages() {
        // no instance
    }

    /**
     * Returns the resource string associated with the given key in the resource bundle. If there isn't 
     * any value under the given key, the key is returned.
     *
     * @param key the resource key
     * @return the string
     */ 
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    /**
     * Returns the resource bundle managed by the receiver.
     * 
     * @return the resource bundle
     */
    public static ResourceBundle getResourceBundle() {
        return RESOURCE_BUNDLE;
    }
    
    /**
     * Returns the formatted resource string associated with the given key in the resource bundle. 
     * <code>MessageFormat</code> is used to format the message. If there isn't  any value 
     * under the given key, the key is returned.
     *
     * @param key the resource key
     * @param arg the message argument
     * @return the string
     */ 
    public static String getFormattedString(String key, Object arg) {
        return getFormattedString(key, new Object[] { arg });
    }
    
    /**
     * Returns the formatted resource string associated with the given key in the resource bundle. 
     * <code>MessageFormat</code> is used to format the message. If there isn't  any value 
     * under the given key, the key is returned.
     *
     * @param key the resource key
     * @param args the message arguments
     * @return the string
     */ 
    public static String getFormattedString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);  
    }   
}
