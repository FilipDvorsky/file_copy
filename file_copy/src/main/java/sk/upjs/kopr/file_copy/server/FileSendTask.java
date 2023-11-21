package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import sk.upjs.kopr.file_copy.FileRequest;

public class FileSendTask implements Runnable {

	private static final int BLOCK_SIZE = 16384; // 16 kB
	private File fileToSend;
	private String LOCATION;
	private final Socket socket;
	private ArrayList<FileRequest> filesList;
	private ArrayList<String> dirsList;
	private int socketPort;

	public FileSendTask(String LOCATION, Socket socket, ArrayList<FileRequest> filesList, ArrayList<String> dirsList)
			throws FileNotFoundException {
		this.LOCATION = LOCATION;
		this.socket = socket;
		this.socketPort = socket.getPort();
		this.filesList = new ArrayList<FileRequest>(filesList);
		this.dirsList = new ArrayList<String>(dirsList);
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			try {
				while (true) {
					String command = ois.readUTF();
					if (command.equals("infoALL")) {
						oos.writeObject(new ArrayList<String>(dirsList));
						oos.flush();
						oos.writeObject(new ArrayList<FileRequest>(filesList));
						oos.flush();
						System.out.println(socketPort+ " sending infoALL");
						break;
					}
					if (command.equals("shutdown")) {
						break;
					} 
					if (command.equals("file")) {
						sendFile(command, oos, ois);
					}
				}
				
			}catch(SocketException e) {
				System.out.println(socketPort+ " connection terminated.");
			}finally {
			
					oos.close();
					ois.close();
				if (socket != null && socket.isConnected())
					System.out.println("shutdown FileSend Tasku pre " +socket.getInetAddress()+ " " + socketPort);
					socket.close();
			}
		} catch (Exception ee) {
			//ee.printStackTrace();
			System.out.println(socketPort+ " connection terminated.");
		}
	}

	private void sendFile(String comm, ObjectOutputStream oos, ObjectInputStream ois)
			throws IOException, ClassNotFoundException, SocketException {

		if (comm.equals("file")) {
			FileRequest fileRequest = (FileRequest) ois.readObject();
			System.out.println(socketPort+ " sending " + fileRequest.getName() );

			try (RandomAccessFile raf = new RandomAccessFile(LOCATION + "\\" + fileRequest.name, "r")) {

				//fileToSend = new File(LOCATION + "\\" + fileRequest.getName());

				raf.seek(fileRequest.offset);
				byte[] buffer = new byte[BLOCK_SIZE];
				for (long send = 0; send < fileRequest.length; send += BLOCK_SIZE) {
					int size = (int) Math.min(BLOCK_SIZE, fileRequest.length - send);
					raf.read(buffer, 0, size);
					oos.write(buffer, 0, size);
				}
				oos.flush();

			}
		}
	}
}