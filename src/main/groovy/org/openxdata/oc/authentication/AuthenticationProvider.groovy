package org.openxdata.oc.authentication

import groovy.util.logging.Log

import org.openxdata.oc.model.OpenClinicaUser
import org.openxdata.oc.util.PropertiesUtil
import org.openxdata.server.admin.model.User
import org.openxdata.server.admin.model.mapping.UserStudyMap
import org.openxdata.server.admin.model.exception.UserNotFoundException

@Log
class AuthenticationProvider {

	private def username
	private def password

	def userService
	def roleService
	def studyService
	def openclinicaService
	def authenticationService

	public User authenticate(String username, String password) {

		log.info("Attempting to authenticate user: ${username}")

		this.username = username
		this.password = password

		def user = null
		
		user = authenticateViaOpenXData()

		if(user){
			return user
		} else {

			user = authenticateViaOpenClinica()
		}
		
		return user
	}

	private def authenticateViaOpenXData() {

		log.info("Attempting to authenticate user: ${username} using openXdata authentication mechanism")

		def existingUser = getOXDUser() 

		if(existingUser) {
			
			log.info("User: ${username} exists in openXdata db. Validating credentials...")
			return authenticationService.authenticate(username, password)
			
		} else {
		
			log.info("User: ${username} doesnot exist in openXdata...")
			return null
		}
	}

	private def getOXDUser() {
		
		try {
			
			return userService.findUserByUsername(username)
			
		} catch (UserNotFoundException ex) {
			return null
		}
	}
	
	private def authenticateViaOpenClinica() {

		log.info("Attempting to fetch user: ${username} from openclinica...")
		
		def user
		def openclinicaUser = openclinicaService.getUserDetails(username)

		if(openclinicaUser) {

			user = createOXDUserFromOpenClinicaUserDetails(openclinicaUser)
		}
		
		return user
	}

	private def createOXDUserFromOpenClinicaUserDetails(def openclinicaUser) {

		log.info("Creating openXdata user with name: ${username} from openclinica user")

		def user = new User()

		user.setClearTextPassword(password)
		user.setName(openclinicaUser.username)

		// Keep the openclinica hashed password in the secret answer field for later use.
		user.setSecretAnswer(openclinicaUser.hashedPassword)

		log.info("Adding mobile role to user: ${username} to enable them use mobile")
		def mobileRole = roleService.getRolesByName("Role_Mobile_User")
		user.addRole(mobileRole.get(0))

		def mappedStudy = createUserStudyMap(user)

		user.setMappedStudies(mappedStudy)

		return userService.saveUser(user)
	}

	private def createUserStudyMap(User user) {
		
		log.info("Mapping study with key: ${getStudyKey()} to user: ${username}");
		
		// Save the user to get hold of the id
		userService.saveUser(user);
		
		def studyKey = getStudyKey();
		
		def study = studyService.getStudyByKey(studyKey);
		def mappedStudy = new HashSet<UserStudyMap>();

		def map = new UserStudyMap();
		
		map.setUser(user);
		map.setStudy(study);
		
		mappedStudy.add(map);
		
		return mappedStudy;
	}
	
	private def getStudyKey() {

		def props = new PropertiesUtil().loadProperties('META-INF/openclinica.properties')

		return props.get("studyOID", "")
	}
}
