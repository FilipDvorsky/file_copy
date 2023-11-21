package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.upjs.kopr.file_copy.FileRequest;


public class Server {

	public static final int SERVER_PORT = 5000;
	public static final String LOCATION = "C:\\Users\\dvors\\Desktop\\KRS\\";
	public static final File FILE_TO_SHARE = new File(LOCATION);
	public static ArrayList<String> dirsList = new ArrayList<String>();
	public static ArrayList<FileRequest> filesList = new ArrayList<FileRequest>();


	public static void main(String[] args) throws IOException {
		System.out.println("List of providable files:");
		System.out.println("-------------------------------------------------");
		printNames(new File(LOCATION));
		System.out.println("-------------------------------------------------");
				
		ExecutorService executor = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(SERVER_PORT)) {
			System.out.println("Sharing folder " + FILE_TO_SHARE);
			System.out.println("Server is running on port " + SERVER_PORT + " ...");

			while(true) {
				Socket socket = ss.accept();
				System.out.println("accepted connection to "+socket.getInetAddress()+" "+socket.getPort());
				FileSendTask fileSendTask = new FileSendTask(LOCATION, socket, filesList, dirsList);
				executor.submit(fileSendTask);
			}
		}
		
	}
	public static void printNames (File dir) {
		if (dir != null && dir.isDirectory ()) {
			File [] files = dir.listFiles ();
		    for (File file : files) {
		    	if (file.isFile()) {
		    		System.out.println(file.getAbsolutePath() + " " + file.getAbsolutePath().replace(LOCATION, ""));
		    		filesList.add(new FileRequest(0, file.length(), file.getAbsolutePath().replace(LOCATION, "")));
		    	}
		    	if (file.isDirectory ()) {
		    		dirsList.add(file.getAbsolutePath().replace(LOCATION, ""));
		    		printNames (file);
		    	}
		    }
		}
	}

}
