package com.canvas.sync;


import com.canvas.sync.controller.SyncController;
import com.canvas.sync.service.SyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
public class SyncControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SyncService syncService;

	@Test
	public void testUnauthenticatedUserCannotPostSync() throws Exception {
		// Mock the behavior of syncContent() to return a non-null Mono<Void>
		when(syncService.syncContent()).thenReturn(Mono.empty());

		// Perform POST request to sync endpoint without providing any authentication
		mockMvc.perform(post("/sync")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"data\": \"someData\" }"))
			// Verify that the response status is 403 Forbidden
			.andExpect(status().isForbidden());
	}
}
