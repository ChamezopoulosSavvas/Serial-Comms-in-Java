package main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ithakimodem.Modem;

public class Main {

	public static void main(String[] args) {
		
		int choice = 0;
		boolean flag = true;
		Scanner ins = null;
		
		System.out.println("Welcome!\nplease choose one option:");
		System.out.println("Press:");
		System.out.println("For Echo: 1");
		System.out.println("From Image With and Without Errors: 2");
		System.out.println("For GPS: 3");
		System.out.println("For AckNack: 4");
		System.out.println("O (zero) to quit");
		
		
		for(;;) {
			ins = new Scanner(System.in);
			System.out.print("Insert Option: ");
			
			do {
				
				choice = ins.nextInt();	
				// this if allows entries with a number of zeros
				// i.e. 03112 to pass.
				if (choice>=0 && choice<=4) flag = false;
				else {
					System.out.println("ERROR: please insert a valid option (1,2)\n");
				}
			}while(flag) ;
			
			flag = true;
			
			if (choice == 0) {
				System.out.println("Exiting...");
				break;
			}
			else if(choice == 1)
				(new Main()).echo(ins);
			else if(choice == 2)
				(new Main()).image(ins);
			else if(choice == 3)
				(new Main()).gps(ins);
			else if(choice == 4)
				(new Main()).ackNack(ins);
		}
		ins.close();
	}

	

	public void echo(Scanner ins) {
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(3000);
		modem.open("ithaki");
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k==-1) break;
				System.out.print((char)k);
				} catch (Exception x) {
					break;
					}
			}
		
		
		System.out.println("Please insert int part of echo_request_code EXXXX: \n");
		
		//Scanner in = new Scanner(System.in);
		int echoRequestCode = 0;
		int countP = 0;
		
		boolean flag = true;
		do {
			
			echoRequestCode = ins.nextInt();
			if (echoRequestCode>=1000 && echoRequestCode<=9999) flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		}while(flag) ;
		
		
		//5 minute loop init
		long startTime = System.currentTimeMillis(), endTime = 0; //for controlling connection time
		long startLoopTime = 0, endLoopTime = 0; //for sampling package transfer time
		long sample = 0; //to save Sample
		int loopCount = 0;
		
		//File creation/opening
		BufferedWriter bw = null;
		try {
			
			File file = new File("data/echo.txt");
			if(!file.exists()) file.createNewFile();
			
			FileWriter wr = new FileWriter(file);
			bw = new BufferedWriter(wr);
			
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		//~5 minute loop
		do {
			
			startLoopTime = System.currentTimeMillis();
			
			modem.write(("E"+Integer.toString(echoRequestCode)+"\r").getBytes());
			
			for(;;) {
				try {
					
					k = modem.read();
					//skip the loop
					if(k == -1) break;
					
					if((char)k == 'P') countP++;
					//The String includes 3 times the letter P
					//The last letter is P.
					//So upon the arrival of the last P
					//its the end of the string
					if(countP == 3) {
						endLoopTime = System.currentTimeMillis();
						sample = endLoopTime - startLoopTime;
						countP = 0;		
					}
					
				} catch(Exception x) {
					break;
				}
			}
			
			//write to file
			try {
				if(bw != null) bw.write(sample + "\n");
			} catch(Exception wr) {
				break;
			}
			
			//Just to see program is running fine
			loopCount++;
			if (loopCount%10 == 0)
				System.out.println("CountLoop:"+ loopCount);
			
			endTime = System.currentTimeMillis();
			
		}while((endTime - startTime)/1000.0 < 5*60);
		
		System.out.println("Echo received and saved.");
	
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		modem.close();	
		//in.close();
		System.out.println("Echo Finished.\n");
		
	}
	
	public void image(Scanner ins) {
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(3000);
		modem.open("ithaki");
		
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k==-1) break;
				System.out.print((char)k);
				} catch (Exception x) {
					break;
					}
			}
		
		
		System.out.println("Please insert int part of \n ERROR FREE image_request_code MXXXX: \n");
		
		//Scanner in = new Scanner(System.in);
		int imageRequestCode = 0;
		int imageErrRequestCode = 0;
		
		boolean flag = true;
		do {
			
			imageRequestCode = ins.nextInt();
			if (imageRequestCode>=1000 && imageRequestCode<=9999) flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		}while(flag) ;
		
		System.out.println("Please insert int part of \n image_request_code WITH ERROR GXXXX: \n");
		flag = true;
		do {
			
			imageErrRequestCode = ins.nextInt();			
			if (imageErrRequestCode>=1000 && imageErrRequestCode<=9999) flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		}while(flag) ;
		//in.close();
		
		
		//File creation/opening
		FileOutputStream fop = null;
		try {
					
			File file = new File("img" + System.currentTimeMillis() + ".jpg");
			if(!file.exists()) file.createNewFile();
					
			fop = new FileOutputStream(file);
					
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
				
		//IMAGE W/O ERRORS
		modem.write(("M" + imageRequestCode + "CAM=PTZ5" + "SIZE=L" + "\r").getBytes());
		System.out.println("Image W/O Errors receiving...");
		
		int i=0;
		for(;;) {
			
			try {
				k=modem.read();
				if (k==-1) break;
				} catch (Exception x) {
					break;
					}
			
			//write to file
				
			try {
				if(fop != null) fop.write(k);
			} catch(Exception wr) {
				break;
			}
				
			//just print a message every now and then so
			//we know its working
			i++;
			if ( i%5000 == 0) System.out.println("Working...");
		}
		
		//Close first image file
		try {
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Image W/O Errors Received");
		
		//Open new image file
		FileOutputStream fop1 = null;
		try {
					
			File file1 = new File("imgErr" + System.currentTimeMillis() + ".jpg");
			if(!file1.exists()) file1.createNewFile();
					
			fop1 = new FileOutputStream(file1);
					
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		modem.write(("G" + imageErrRequestCode + "CAM=PTZ5" + "SIZE=L" + "\r").getBytes());
		System.out.println("Image W/ Errors receiving...");
		
		for(;;) {
			
			try {
				k=modem.read();
				if (k==-1) break;
				} catch (Exception x) {
					break;
					}
			
			//write to file
			try {
				if(fop1 != null) fop1.write(k);
			} catch(Exception wr) {
				break;
			}
				
			//just print a message every now and then so
			//we know its working
			i++;
			if ( i%5000 == 0) System.out.println("Working...");
		}
		
		System.out.println("Attemtping Close");
		try {
			fop1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Image W/O Errors Received");
		
		modem.close();
		System.out.println("Image finished.\n");
	}
	
	public void gps(Scanner ins) {

		int k;
		Modem modem;
		modem = new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(3000);
		modem.open("ithaki");

		String s = "", time = "", latitude = "", longitude = "";
		ArrayList<String> times = new ArrayList<String>();
		ArrayList<String> lats = new ArrayList<String>();
		ArrayList<String> longs = new ArrayList<String>();

		ArrayList<String> finalLats = new ArrayList<String>();
		ArrayList<String> finalLongs = new ArrayList<String>();

		// read last coordinate from file
		BufferedReader reader;
		String line = "", lastLat = "", lastLong = "";

		int i = 0, temp = 0, lastLatDeg = 0, lastLatMin = 0, lastLongDeg = 0, lastLongMin = 0;
		int lastLatSec = 0, lastLongSec = 0;
		double temp1 = 0;

		int index = 0;
		try {
			File file = new File("/home/chamezos/eclipse-workspace/Networks_1_Project/points.txt");
			if (!file.exists())
				file.createNewFile();

			reader = new BufferedReader(new FileReader(file));

			line = reader.readLine();
			while (line != null) {

				if (index % 2 == 0) {
					finalLats.add(line);
					lastLat = line;
				} else {
					finalLongs.add(line);
					lastLong = line;
				}
				index++;
				System.out.println("Lines Read:" + index);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (index != 0) {

			System.out.println("Last Latitude: " + lastLat);
			System.out.println("Last Longitude: " + lastLong);

			temp1 = Double.parseDouble(lastLat);
			temp = (int) temp1;

			lastLatDeg = temp / 100;
			lastLatMin = temp % 100;
			temp1 = temp1 - (int) temp1;
			temp1 = temp1 * 60;
			lastLatSec = (int) temp1;

			temp1 = Double.parseDouble(lastLong);
			temp = (int) temp1;

			lastLongDeg = temp / 100;
			lastLongMin = temp % 100;
			temp1 = temp1 - (int) temp1;
			temp1 = temp1 * 60;
			lastLongSec = (int) temp1;

		} else
			System.out.println("No Old Coordinates.");

		// initial message
		for (;;) {
			try {
				k = modem.read();
				if (k == -1)
					break;
				System.out.print((char) k);
			} catch (Exception x) {
				break;
			}
		}

		System.out.println("Please insert int part of gps_request_code PXXXX: \n");

		int gpsRequestCode = 1524;

		boolean flag = true;
		do {

			gpsRequestCode = ins.nextInt();

			// this if allows entries with a number of zeros // i.e. 03112 to pass.
			if (gpsRequestCode >= 1000 && gpsRequestCode <= 9999)
				flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		} while (flag);

		modem.write(("P" + gpsRequestCode + "R=1011199" + "\r").getBytes());

		for (;;) {

			try {
				k = modem.read();
				if (k == -1)
					break;
			} catch (Exception x) {
				break;
			}

			s += (char) k;

			// store and forget line
			if (s.contains("\r")) {
				if (time != "")
					times.add(time);
				if (latitude != "")
					lats.add(latitude);
				if (longitude != "")
					longs.add(longitude);

				s = "";
				index = 0;
				time = "";
				latitude = "";
				longitude = "";

			} else if (s.contains("GPGGA")) {
				index++;
				if (index > 2 && index < 9)
					time += (char) k;
				else if (index > 13 && index < 23)
					latitude += (char) k;
				else if (index > 25 && index < 36)
					longitude += (char) k;
			}
		}

		int degreesLat, minutesLat, degreesLong, minutesLong;
		int secondsLat, secondsLong;

		boolean isFar = false;

		if (finalLats.size() < 5) {

			for (i = 0; i < lats.size() && finalLats.size() < 5; i++) {

				if (!finalLats.contains(lats.get(i))) {

					temp1 = Double.parseDouble(lats.get(i));

					System.out.println(temp1);
					temp = (int) temp1;
					degreesLat = temp / 100;
					minutesLat = temp % 100;

					temp1 = temp1 - (int) temp1;
					temp1 = temp1 * 60;
					secondsLat = (int) temp1;

					temp1 = Double.parseDouble(longs.get(i));
					temp = (int) temp1;
					degreesLong = temp / 100;
					minutesLong = temp % 100;

					temp1 = temp1 - (int) temp1;
					temp1 = temp1 * 60;
					secondsLong = (int) temp1;

					if (finalLats.size() == 0) {

						finalLats.add(lats.get(i));
						finalLongs.add(longs.get(i));

						lastLatDeg = degreesLat;
						lastLatMin = minutesLat;
						lastLatSec = secondsLat;

						lastLongDeg = degreesLong;
						lastLongMin = minutesLong;
						lastLongSec = secondsLong;

					} else {

						// check distance using eucledean distance
						if (Math.sqrt(
								Math.pow(lastLatDeg - degreesLat, 2) + Math.pow(lastLongDeg - degreesLong, 2)) > 0)
							isFar = true;
						else if (Math.sqrt(
								Math.pow(lastLatMin - minutesLat, 2) + Math.pow(lastLongMin - minutesLong, 2)) > 0)
							isFar = true;
						else if (Math.sqrt(
								Math.pow(lastLatSec - secondsLat, 2) + Math.pow(lastLongSec - secondsLong, 2)) >= 4)
							isFar = true;
					}

					if (isFar) {

						finalLats.add(lats.get(i));
						finalLongs.add(longs.get(i));

						lastLatDeg = degreesLat;
						lastLatMin = minutesLat;
						lastLatSec = secondsLat;

						lastLongDeg = degreesLong;
						lastLongMin = minutesLong;
						lastLongSec = secondsLong;

						isFar = false;

					}
				}
			}
		}

		// File creation/opening for storing new points, if any.
		BufferedWriter bw = null;
		try {

			File file = new File("/home/chamezos/eclipse-workspace/Networks_1_Project/points.txt");
			if (!file.exists())
				file.createNewFile();

			FileWriter wr = new FileWriter(file, false);
			bw = new BufferedWriter(wr);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		for (i = 0; i < finalLats.size(); i++) {
			// write to file
			try {
				if (bw != null)
					bw.write(finalLats.get(i) + "\n");
				if (bw != null)
					bw.write(finalLongs.get(i) + "\n");
			} catch (Exception wr) {
				break;
			}
		}

		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// gps images receive
		line = "";
		line = "P" + Integer.toString(gpsRequestCode);

		for (i = 0; i < finalLats.size(); i++) {

			temp1 = Double.parseDouble(finalLats.get(i));

			System.out.println("temp1: " + temp1);
			temp = (int) temp1;
			degreesLat = temp / 100;
			minutesLat = temp % 100;
			temp1 = temp1 - (int) temp1;
			temp1 = temp1 * 60;
			secondsLat = (int) temp1;

			temp1 = Double.parseDouble(finalLongs.get(i));

			temp = (int) temp1;
			degreesLong = temp / 100;
			minutesLong = temp % 100;

			temp1 = temp1 - (int) temp1;
			temp1 = temp1 * 60;
			secondsLong = (int) temp1;

			line += "T=" + Integer.toString(degreesLong) + Integer.toString(minutesLong);
			if (secondsLong < 10) {
				line += "0" + Integer.toString(secondsLong);
			} else
				line += Integer.toString(secondsLong);

			line += Integer.toString(degreesLat) + Integer.toString(minutesLat);
			if (secondsLat < 10) {
				line += "0" + Integer.toString(secondsLat);
			} else
				line += Integer.toString(secondsLat);

		}

		line += "\r";
		System.out.println("Line: " + line);

		// File creation/opening
		FileOutputStream fop = null;
		try {

			File file = new File("GPSimg" + System.currentTimeMillis() + ".jpg");
			if (!file.exists())
				file.createNewFile();

			fop = new FileOutputStream(file);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		modem.write(line.getBytes());
		System.out.println("GPS Image receiving...");

		for (;;) {

			try {
				k = modem.read();
				if (k == -1)
					break;
			} catch (Exception x) {
				break;
			}

			// write to file
			try {
				if (fop != null)
					fop.write(k);
			} catch (Exception wr) {
				break;
			}

			// just print a message every now and then so
			// we know its working
			i++;
			if (i % 30000 == 0)
				System.out.println("Working...");
		}

		try {
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("GPS Image Received");

		modem.close();
	}
	
	public void ackNack(Scanner ins) {

		int k;
		Modem modem;
		modem = new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(1000);
		modem.open("ithaki");

		// initial message
		for (;;) {
			try {
				k = modem.read();
				if (k == -1)
					break;
				System.out.print((char) k);
			} catch (Exception x) {
				break;
			}
		}

		int ackResultCode = 0;
		int nackResultCode = 0;

		System.out.println("Please insert int part of ack_result_code QXXXX: \n");

		boolean flag = true;
		do {

			ackResultCode = ins.nextInt();

			if (ackResultCode >= 1000 && ackResultCode <= 9999)
				flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		} while (flag);

		System.out.println("Please insert int part of nack_result_code RXXXX: \n");
		flag = true;
		do {

			nackResultCode = ins.nextInt();

			if (nackResultCode >= 1000 && nackResultCode <= 9999)
				flag = false;
			else {
				System.out.println("ERROR: please insert 4-digit integer\n");
			}
		} while (flag);

		// File creation/opening
		BufferedWriter bw = null;
		try {

			File file = new File("receiveTimes.txt");
			if (!file.exists())
				file.createNewFile();

			FileWriter wr = new FileWriter(file);
			bw = new BufferedWriter(wr);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		boolean correct = true;
		long processStartTime = 0, processEndTime = 0;
		long recvStartTime = 0, recvEndTime = 0;
		int reps = 0, val = 0;
		ArrayList<Integer> repetitionTimes = new ArrayList<Integer>();
		
		int totalRequests = 0, incorrectRequests = 0;

		int XXXX[] = new int[16];
		int index = 0;

		String FCS = "";
		int xor = 0;

		processStartTime = System.currentTimeMillis();
		do {

			index = 0;
			FCS = "";
			xor = 0;
			for (int i = 0; i < 16; i++) {
				XXXX[i] = 0;
			}

			if (correct) {
				reps = 0;
				recvStartTime = System.currentTimeMillis();
				// send ACK
				modem.write(("Q" + ackResultCode + "\r").getBytes());
			} else
				// send NACK
				modem.write(("R" + nackResultCode + "\r").getBytes());

			for (;;) {

				try {
					k = modem.read();

					if (k == -1)
						break;
					;
				} catch (Exception x) {
					break;
				}

				index++;

				if (index > 31 && index < 48) {
					XXXX[index - 32] = k;
				} else if (index > 49 && index < 53)
					FCS += (char) k;

			}

			for (int i = 0; i < 16; i++) {
				xor = xor ^ XXXX[i];
			}

			System.out.println("xor: " + xor);
			System.out.println("FCS: " + FCS);

			int iFCS = 0;
			if (FCS != "")
				iFCS = Integer.parseInt(FCS);

			if (xor == iFCS) {
				correct = true;
				System.out.println("Correct Package");
				recvEndTime = System.currentTimeMillis();
				
				// write to file
				try {
					if (bw != null)
						bw.write((recvEndTime - recvStartTime) + "\n");
				} catch (Exception wr) {
					break;
				}

				System.out.println("Receive time: " + (recvEndTime - recvStartTime));
			} else {
				correct = false;
				incorrectRequests++;
				reps++;
				System.out.println("Incorrect Package");
			}
			
			if(repetitionTimes.size() < (reps + 1))
				for(int i = 0; i < (reps + 1); i++) 
					repetitionTimes.add(0);					
				
			val = repetitionTimes.get(reps);
			val++;
			repetitionTimes.set(reps, val);
			
			totalRequests++;
			processEndTime = System.currentTimeMillis();

		} while ((processEndTime - processStartTime) / 1000 <= 5*60);

		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		double BER = ((double) incorrectRequests) / ((double) totalRequests);
		System.out.println("BER: " + BER);

		// write Requests
		try {

			File file = new File("Requests.txt");
			if (!file.exists())
				file.createNewFile();

			FileWriter wr = new FileWriter(file);
			bw = new BufferedWriter(wr);

			if (bw != null)
				bw.write("Total Requests: " + totalRequests + "\n");
			if (bw != null)
				bw.write("Incorrect Requests: " + incorrectRequests + "\n");
			if (bw != null)
				bw.write("BER: " + BER + "\n");
			
			for(int i = 0; i < repetitionTimes.size(); i++)
				bw.write("Repetitions: " + i + " -> " + repetitionTimes.get(i) + "\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("Total Requests: " + totalRequests);
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("ACK-NACK done.");
		modem.close();

	}
	
}
