package gc.apiClient;

import java.io.File;
import java.io.IOException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogArchiver {
	
	  @Scheduled(cron = "0 0 0 * * *") //하루 한번 정각에 
	//@Scheduled(cron = "0 */3 * * * *") // Every 3 minutes
    //@Scheduled(cron = "0 * * * * *") // Every minute
	//@Scheduled(cron = "*/5 * * * * *") // Every 5second
	 
    public void archiveLogs() {
        try {
        	ProcessBuilder processBuilder = new ProcessBuilder("bash", "/logs/archive_logs.sh");
            processBuilder.directory(new File(System.getProperty("user.dir"))); // Set working directory
            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("Log archiving script executed successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
