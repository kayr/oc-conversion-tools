package org.openxdata.oc

import org.openxdata.oc.model.BindSet

/**
 * Encapsulates utilities needed by the Transformation class to perform its duties well. 
 * The reason of separating them into a different class is to avoid the trap of having big classes and also to separate concerns.
 *
 */
public class TransformUtil {

	public def loadFile(def fileName){
		if(fileName){
			return new File(getClass().getResource(fileName).getFile())
		}
		else{
			throw new IllegalArgumentException("File Name cannot null or empty.")
		}
	}

	public def loadFileContents(def fileName){

		def file = loadFile(fileName)

		def builder = new StringBuilder()
		file.eachLine{ builder.append(it) }

		return builder.toString()
	}

	public def hasDuplicateBindings(def docWithDuplicateBindings) {

		if (getDuplicateBindings(docWithDuplicateBindings).isEmpty())
			return false

		return true
	}

	public def getDuplicateBindings(def docWithDuplicateBindings) {

		def bindings = docWithDuplicateBindings.breadthFirst().bind.findAll{it.@id}.'@id'
		def set = new BindSet<String>()
		
		bindings.each {
			set.add(it)
		}

		return set.duplicatedBindings
	}
}
