package sk.upjs.kopr.file_copy.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import sk.upjs.kopr.file_copy.FileRequest;

public class FileReceiveTask implements Runnable {
	private static final int BUFFER_SIZE = 16384;
	private BlockingQueue<FileRequest> filesQueue;
	private String saveLocation;
	private CountDownLatch countLatch;
	private boolean inter;
	private Socket socket;
	private int socketPort;
	

	public FileReceiveTask(BlockingQueue<FileRequest> filesQueue, InetAddress inetAddress, int serverPort,
			String saveLocation, CountDownLatch countLatch) throws IOException {
		this.filesQueue = filesQueue;
		this.saveLocation = saveLocation;
		this.countLatch = countLatch;
		this.socket = new Socket(inetAddress, serverPort);
		this.socketPort = socket.getLocalPort();
	}
	
	 @Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			
			while (true) {
				if ((inter == true) && Thread.currentThread().isInterrupted()) {
					inter = true;
					break;
				}
				FileRequest fileRequest = filesQueue.poll();

				if (fileRequest == null) {
					oos.writeUTF("shutdown");
					break;
				} else {
					receiveFile(fileRequest, ois, oos, socket);
				}
			}
			
			oos.close();
			ois.close();
			System.out.println(socketPort+ " is Shutting down.");
			socket.close();
			countLatch.countDown();
		} catch (IOException ee) {
			System.out.println(socketPort+" connection terminated.");
			//ee.printStackTrace();
		}catch(Exception e){
			System.out.println(socketPort+" connection terminated.");
			//e.printStackTrace();
		}finally {
			if (inter)
				Thread.currentThread().interrupt();
		}
	}
	


	private void receiveFile(FileRequest filesReq, ObjectInputStream ois, ObjectOutputStream oos, Socket socket)
			throws IOException {

		oos.writeUTF("file");
		oos.flush();
		oos.writeObject(filesReq);
		oos.flush();
		System.out.println(socketPort + " Getting " + filesReq.getName());

		int tempOffset = 0;
		long fileOffset = filesReq.getOffset();
		
		try (RandomAccessFile raf = new RandomAccessFile(saveLocation + filesReq.getName(), "rw")){
			byte[] bytes = new byte[BUFFER_SIZE];
			raf.seek(fileOffset);
			while (fileOffset < filesReq.getLength()) {

				if (filesReq.getLength() - fileOffset < bytes.length) {
					tempOffset = ois.read(bytes, 0,  (int)(filesReq.getLength() - fileOffset));
				} else {
					tempOffset = ois.read(bytes, 0, bytes.length);
				}

				raf.seek(fileOffset);
				raf.write(bytes, 0, tempOffset);
				fileOffset += tempOffset;
				
				if (Thread.currentThread().isInterrupted()) {
					inter = true;
					break;
				}
			}
			raf.close();
		}catch (SocketException e) {
			System.out.println(socketPort+ " connection terminated.");
		}
	}
}
