package eu.cessda.cmv.server;

import static eu.cessda.cmv.core.ValidationGateName.BASIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gesis.commons.resource.Resource;
import org.gesis.commons.test.DefaultTestEnv;
import org.gesis.commons.test.TestEnv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncListenableTaskExecutor;

import eu.cessda.cmv.core.ValidationService;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = RANDOM_PORT )
class ExperimentalTest
{
	@Test
	void asyncValidate(
			@Autowired AsyncListenableTaskExecutor taskExecutor,
			@Autowired ValidationService.V10 validationService,
			@Autowired List<Resource.V10> demoDocuments,
			@Autowired List<Resource.V10> demoProfiles )
	{
		TestEnv.V14 testEnv = DefaultTestEnv.newInstance();
		List<ValidationReportV0> validationReports = new ArrayList<>();
		demoDocuments.forEach( demoDocument ->
		{
			Callable<ValidationReportV0> command = () -> validationService.validate( demoDocument,
					demoProfiles.get( 0 ), BASIC );
			taskExecutor.submitListenable( command )
					.addCallback( validationReport ->
					{
						validationReports.add( validationReport );
					}, exception ->
					{
						throw new IllegalArgumentException( exception );
					} );
		} );
		testEnv.sleepSeconds( demoDocuments.size() );
		assertThat( validationReports, hasSize( demoDocuments.size() ) );
	}
}
