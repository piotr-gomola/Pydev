/*
 * Created on Nov 12, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion.revisited;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.python.parser.SimpleNode;
import org.python.pydev.parser.PyParser;
import org.python.pydev.plugin.PydevPlugin;

/**
 * @author Fabio Zadrozny
 */
public abstract class AbstractModule implements Serializable{

    /**
     * @return tokens for the wild imports.
     */
    public abstract IToken[] getWildImportedModules();
    
    /**
     * @return tokens for the imports in the format from xxx import yyy
     * or import xxx 
     */
    public abstract IToken[] getTokenImportedModules();
    
    /**
     * This function should get all that is present in the file as global tokens.
     * Note that imports should not be treated by this function (imports have their own functions).
     * 
     * @return
     */
    public abstract IToken[] getGlobalTokens();
    
    /**
     * This function should return all tokens that are global for a given token.
     * E.g. if we have a class declared in the module, we return all tokens that are 'global'
     * for the class (methods and attributes).
     * 
     * @param token
     * @return
     */
    public abstract IToken[] getGlobalTokens(String token);
    
    /**
     * 
     * @return the docstring for a module.
     */
    public abstract String getDocString();
    
    
    /**
     * Name of the module
     */
    protected String name;
   
    /**
     * Constructor
     * 
     * @param name - name of the module
     */
    protected AbstractModule(String name){
        this.name = name;
    }
    
    /**
     * This method creates a source module from a file.
     * 
     * @param f
     * @return
     * @throws FileNotFoundException
     */
    public static AbstractModule createModule(String name, File f) throws FileNotFoundException {
        String path = f.getAbsolutePath();
        if(PythonPathHelper.isValidFileMod(path)){
	        if(isValidSourceFile(path)){
	            FileInputStream stream = new FileInputStream(f);
	            try {
	                int i = stream.available();
	                byte[] b = new byte[i];
	                stream.read(b);
	
	                Document doc = new Document(new String(b));
                    return createModuleFromDoc(name, f, doc);
	
	            } catch (IOException e) {
                    PydevPlugin.log(e);
                } finally {
	                try {
                        stream.close();
                    } catch (IOException e1) {
                        PydevPlugin.log(e1);
                    }
	            }
	        }else{ //this should be a compiled extension... we have to get completions from the python shell.
	            return new CompiledModule(name);
	        }
        }
        
        //if we are here, return null...
        return null;
    }

    
    /**
     * @param path
     * @return
     */
    private static boolean isValidSourceFile(String path) {
        return path.endsWith(".py") || path.endsWith(".pyw");
    }
    
    /**
     * @param name
     * @param f
     * @param doc
     * @return
     */
    public static AbstractModule createModuleFromDoc(String name, File f, IDocument doc) {
        //for doc, we are only interested in python files.
        String absolutePath = f.getAbsolutePath();
        if(isValidSourceFile(absolutePath)){
	        Object[] obj = PyParser.reparseDocument(doc, true);
	        SimpleNode n = (SimpleNode) obj[0];
	        return new SourceModule(name, f, n);
        }else{
            return null;
        }
    }

    /**
     * Creates a source file generated only from an ast.
     * @param n
     * @return
     */
    public static AbstractModule createModule(SimpleNode n) {
        return new SourceModule(null, null, n);
    }
    /**
     * @param m
     * @param f
     * @return
     */
    public static AbstractModule createEmptyModule(String m, File f) {
        return new EmptyModule(m, f);
    }

}
