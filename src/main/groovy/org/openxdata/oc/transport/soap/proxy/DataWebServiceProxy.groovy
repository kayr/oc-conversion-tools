package org.openxdata.oc.transport.soap.proxy

import groovy.util.logging.Log

import org.openxdata.oc.exception.ImportException
import org.openxdata.oc.model.OpenClinicaUser
import org.openxdata.oc.transport.HttpTransportHandler
import org.openxdata.oc.transport.soap.SoapRequestProperties

@Log
class DataWebServiceProxy extends SoapRequestProperties {

	def envelope

	@Override
	public def getSoapEnvelope(def username) {
		envelope = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://openclinica.org/ws/data/v1">
							${getHeader()}
						<soapenv:Body>
							<v1:findUserRequest>
								<v1:username>${username}</v1:username>
							</v1:findUserRequest>
						</soapenv:Body>
					 </soapenv:Envelope>"""
	}

	def getUserDetails(def username) {

		if(username.size() > 0) {

			log.info("Fetching user details for: ${username}")
			envelope = getSoapEnvelope(username)

			def transportHandler = new HttpTransportHandler(envelope:envelope)
			def response = transportHandler.sendRequest(connectionFactory.getStudyConnection())

			def user = extractResponse(response)

			return user
		} else {
			throw new ImportException("Username cannot be null or empty")
		}
	}

	private def extractResponse(httpResonse) {

		def response = httpResonse.depthFirst().findUserResponse[0]

		if(response.result.text().equals("Fail")){

			def resp = response.error.text()
			log.info(resp)
			throw new ImportException(resp)
		} else {

			return new OpenClinicaUser(response)
		}
	}
}
