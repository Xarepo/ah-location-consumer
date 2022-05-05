package eu.arrowhead.application.skeleton.consumer.service;

import org.springframework.stereotype.Service;
import eu.arrowhead.application.skeleton.consumer.Domain.Coordinates;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import ai.aitia.arrowhead.application.library.ArrowheadService;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@Service
public class LocationService {

	//=================================================================================================
	// members

	@Autowired
	private ArrowheadService arrowheadService;

	//=================================================================================================
	// methods

	public Coordinates fetchLocation() {

		final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();

		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("location");

		orchestrationFormBuilder.requestedService(requestedService)
				.flag(Flag.MATCHMAKING, false)
				.flag(Flag.OVERRIDE_STORE, false)
				.flag(Flag.TRIGGER_INTER_CLOUD, false);

		final OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder.build();

		OrchestrationResponseDTO response = null;
		try {
			response = arrowheadService.proceedOrchestration(orchestrationRequest);
		} catch (final ArrowheadException ex) {
			ex.printStackTrace();
		}

		if (response == null || response.getResponse().isEmpty()) {
			System.out.println("Orchestration response is empty");
			return null;
		}

		final OrchestrationResultDTO result = response.getResponse().get(0);

		final HttpMethod httpMethod = HttpMethod.GET;
		final String address = result.getProvider().getAddress();
		final int port = result.getProvider().getPort();
		final String serviceUri = result.getServiceUri();
		final String interfaceName = result.getInterfaces().get(0).getInterfaceName();
		String token = null;

		if (result.getAuthorizationTokens() != null) {
			token = result.getAuthorizationTokens().get(interfaceName);
		}

		final Object payload = null;

		try {
			final Map<String, Double> coordinates = arrowheadService.consumeServiceHTTP(Map.class,
					httpMethod, address, port, serviceUri, interfaceName, token, payload);
			if (coordinates == null)
				return null;
			final double latitude = coordinates.get("latitude");
			final double longitude = coordinates.get("longitude");
			return new Coordinates(latitude, longitude);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
