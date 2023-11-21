package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.FileRequest;

public class Client extends Service<Boolean>{
	
	private static final String SERVER_HOST = "localhost"; //server ip
	private static final int SERVER_PORT = 5000;
	private static String FILE_TO_SAVE = "C:\\Users\\dvors\\Desktop\\bruh\\"; // download location
	private static int numberOfThreads;
	private static ArrayList<String> dirsList;
	private static ArrayList<FileRequest> filesList;
	private static BlockingQueue<FileRequest> filesQueue = new LinkedBlockingDeque<>();
	private CountDownLatch countLatch; 
	private ExecutorService executor;

	public Client(int numberOfThreads, ExecutorService executor) {
		this.numberOfThreads = numberOfThreads;
		System.out.println("numberOfThreads: "+numberOfThreads);
		countLatch = new CountDownLatch(numberOfThreads);
		this.executor = executor;
	}
	
	@Override
	protected Task<Boolean> createTask() {

		return new Task<Boolean>() {
		
			@SuppressWarnings("unchecked")
			@Override
			protected Boolean call() throws Exception {
				//getting info about what server provides
				try(Socket socket = new Socket(InetAddress.getByName(SERVER_HOST), SERVER_PORT)) {
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					System.out.println(socket.getLocalPort()+" Attempting to get Info data");
					oos.writeUTF("infoALL");
					oos.flush();
					dirsList = (ArrayList<String>) ois.readObject();
					filesList = (ArrayList<FileRequest>) ois.readObject();
					System.out.println(dirsList.toString());
					System.out.println(filesList.toString());
					oos.writeUTF("shutdown");
					socket.close();
				}
				
				//making of missing dirs
				for (String dir : dirsList) {
					String dirloc = FILE_TO_SAVE+dir;
					File f = new File(dirloc);
					if (!f.exists()) {
						f.mkdir();
						System.out.println("Creating folder: "+dirloc);
					}
				}
				
				//checking state of downloaded data
				for(FileRequest fileReq: filesList) {
					String filename = fileReq.getName();
					long filelength = fileReq.getLength();
					File f = new File(FILE_TO_SAVE+filename);
					if (f.exists()) {
						long offset = f.length();
						if (offset != fileReq.getLength()) {
							filesQueue.put(new FileRequest(offset, filelength, filename));
							System.out.println(offset+" "+ filelength+" "+filename);
						}
					}else {
						filesQueue.put(new FileRequest(0, filelength, filename));
					}
				}		
				
				
				// zacat vyrabat tasky pre executor (v pocte tcp spojeny zadanych)

				for (int i = 0; i < numberOfThreads; i++) {
					FileReceiveTask fileReceiveTask = new FileReceiveTask(filesQueue, InetAddress.getByName(SERVER_HOST), SERVER_PORT, FILE_TO_SAVE, countLatch);
					executor.submit(fileReceiveTask);
				}
				
				try {
					countLatch.await();
				}catch (Exception e) {
					executor.shutdownNow();
				}
				executor.shutdown();
				System.out.println("client is shutting down");
				updateValue(true);
				return true;
			}
		};
	}
}
