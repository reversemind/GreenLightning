package com.ociweb.gl.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.MsgRuntime;
import com.ociweb.pronghorn.pipe.ChannelWriter;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;

/**
 * Scans up the object tree to find instances of specific classes
 * @author Nathan Tippy
 *
 */
public class ChildClassScanner {

	private static final Logger logger = LoggerFactory.getLogger(ChildClassScanner.class);
	
	public static boolean notPreviouslyHeld(Object child, 
			                                   Object topParent, 
			                                   IntHashTable usageChecker) {
	    int hash = System.identityHashCode(child);
	    int parentHash = System.identityHashCode(topParent);           
	    if (IntHashTable.hasItem(usageChecker, hash)) {
	    	if (parentHash!=IntHashTable.getItem(usageChecker, hash)) {
	    		return false;
	    	}
	    } 
	    //keep so this is detected later if use
	    IntHashTable.setItem(usageChecker, hash, parentHash);
	    return true;
	}
	///////////
	///////////

	static boolean visitByClass(Object listener, int depth, 
												 ChildClassScannerVisitor visitor,
												 Class<? extends Object> c,
												 Class targetType, 
												 Object topParent,
												 Collection<Object> seen) {
			
			Field[] fields = c.getDeclaredFields();
	                        
	        int f = fields.length;
	        while (--f >= 0) {
	      
	                fields[f].setAccessible(true);   
	                Object obj = null;
					try {
						obj = fields[f].get(listener);
			
						if (targetType.isInstance(obj)) {
							if (!visitor.visit(obj, topParent)) {
								return false;
							}                              
						} else {      
							//NOTE: using the TrieParser would be better here.... (faster startup)
							String name;
							if (    (obj!=null)
									&& (obj.getClass()!=null)
									&&  (!obj.getClass().isPrimitive()) 
									&& (obj != listener) 
									&& (!(name=obj.getClass().getName()).startsWith("java.")) 
									&& (!name.startsWith("[Ljava."))
									&& (!name.startsWith("[["))
									&& (!name.startsWith("[I"))
									&& (!name.startsWith("[B"))
									&& (!name.startsWith("[J"))
									&& (!name.startsWith("[S"))
									
									&& (!name.startsWith("com.ociweb.pronghorn.stage."))
									&& (!name.startsWith("[Lcom.ociweb.pronghorn.stage."))
									
									&& (!name.startsWith("com.ociweb.iot.hardware."))
									&& (!name.startsWith("[Lcom.ociweb.iot.hardware."))
																	
									&& (!name.startsWith("com.ociweb.pronghorn.pipe."))  
									&& (!name.startsWith("[Lcom.ociweb.pronghorn.pipe.")) 
									
									&& (!name.startsWith("com.ociweb.pronghorn.util."))  
									&& (!name.startsWith("[Lcom.ociweb.pronghorn.util.")) 
									
									&& (!name.startsWith("com.ociweb.pronghorn.network."))  
									&& (!name.startsWith("[Lcom.ociweb.pronghorn.network.")) 
									
									&& (!name.startsWith("org.slf4j."))
									&& (!obj.getClass().isEnum())
									&& (!(obj instanceof MsgRuntime))  
									&& (!(obj instanceof ChannelWriter))  
									&& (!(obj instanceof BuilderImpl)) 
									
									&& !fields[f].isSynthetic()
									&& fields[f].isAccessible() 
									&& depth<=11) { //stop recursive depth
								
								if (!seen.contains(obj)) {
									seen.add(obj);
								                		//if (depth <3 && obj.getClass().getName().startsWith("[")) {
								                		//	logger.info(depth+" "+obj.getClass().getName());
								                		//}
								
								//recursive check for command channels
									if (!visitUsedByClass(obj, depth+1, visitor, topParent, targetType, seen)) {
										return false;
									}
								}
							}
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}                                
	   
	        }
	        return true;
		}

	static boolean visitUsedByClass(Object obj, int depth, ChildClassScannerVisitor visitor, 
			     Object topParent, Class targetType, Collection<Object> seen) {
		
	    if (null!=obj) {
		    Class<? extends Object> c = obj.getClass();
		    while (null != c) {
		    	if (!visitByClass(obj, depth, visitor, c, targetType, topParent, seen)) {
		    		return false;
		    	}
		    	c = c.getSuperclass();
		    }
	    }
	    return true;
	}

	public static boolean visitUsedByClass(Object listener, ChildClassScannerVisitor visitor, Class target) {
	
		return visitUsedByClass(listener, 0, visitor, listener, target, new ArrayList<Object>());
	}

}
