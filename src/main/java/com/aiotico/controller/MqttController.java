package com.aiotico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aiotico.MqttGateway;
import com.aiotico.dto.MqttPayload;

@RestController
public class MqttController {
	@Autowired
	MqttGateway mqtGateway;

	@PostMapping("/sendMessage")
	public ResponseEntity<?> publish(@RequestBody MqttPayload mqttPayload) {
/*
		try {
			JsonObject convertObject = new Gson().fromJson(mqttMessage, JsonObject.class);
			mqtGateway.senToMqtt(convertObject.get("message").toString(), convertObject.get("topic").toString());
			return ResponseEntity.ok("Success");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.ok("fail");
		}
		*/
		
		 try {
	            mqtGateway.senToMqtt(mqttPayload.getMessage(), mqttPayload.getTopic());
	            return ResponseEntity.ok("Success");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
	        }

	}

}
