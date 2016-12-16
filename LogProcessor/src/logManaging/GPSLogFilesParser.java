package logManaging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDateTime;

import commoninterface.dataobjects.GPSData;

/**
 * Read NMEA GPS log files and convert them into GPSData objects for each robot
 * 
 * @author Vasco Costa
 *
 */
public class GPSLogFilesParser {
	private final static String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private final String FILE_PREFIX = "GPSLog_";
	private final String PARSED_DATA_FILE_GPS = "mergedLogs_gps.log";

	private File inputFolderFile;
	private HashMap<Integer, List<GPSData>> gpsData = new HashMap<Integer, List<GPSData>>();

	public GPSLogFilesParser() throws FileNotFoundException {
		this(INPUT_FOLDER);
	}

	public GPSLogFilesParser(String inputFolderPath) throws FileNotFoundException {
		inputFolderFile = new File(inputFolderPath);

		if (inputFolderFile.exists() && inputFolderFile.isDirectory()) {
			for (File folder : inputFolderFile.listFiles()) {
				if (folder.isDirectory()) {

					FilenameFilter filenameFilter = new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith(FILE_PREFIX);
						}
					};
					File[] fileList = folder.listFiles(filenameFilter);

					if (fileList.length == 1) {
						System.out.printf("[%s] Parsing %s%n", getClass().getSimpleName(),
								fileList[0].getAbsolutePath());

						int currentRobot = Integer.parseInt(folder.getName());
						List<GPSData> data = parseNMEAData(fileList[0]);
						gpsData.put(currentRobot, data);

						if (data.size() > 0) {
							System.out.printf("[%s] -----> %d GPRMC sentences parsed%n", getClass().getSimpleName(),
									data.size());
						}
					} else {
						System.err.printf("[%s] Ambiguous or missing GPS log file%n", getClass().getSimpleName());
					}
				}
			}
		} else {
			throw new FileNotFoundException("Input folder does not exist");
		}
	}

	/**
	 * Parses the NMEA messages send by the GPS receiver, containing the
	 * Navigation information
	 * 
	 * @param data
	 *            : NMEA sentence to be processed
	 */
	private List<GPSData> parseNMEAData(File inputFile) {
		ArrayList<GPSData> data = new ArrayList<GPSData>();
		int parsingErrors = 0;

		FileReader fileReader = null;
		BufferedReader inputBuffReader = null;
		try {
			fileReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(fileReader);

			String line = "";
			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.startsWith("#")) {
					int indexComma = line.indexOf(',');

					if (indexComma >= 0 && line.substring(0, indexComma).equals("$GPRMC")) {
						try {
							String[] split = line.split(",");
							data.add(parseGPRMCSentence(split));
						} catch (RuntimeException e) {
							// System.err.printf("[%s] Error parsing %s%n",
							// getClass().getSimpleName(), line);
							parsingErrors++;
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				}
			}

			if (inputBuffReader != null) {
				try {
					inputBuffReader.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				}
			}
		}

		if (parsingErrors > 0)
			System.out.printf("[%s] -----> %d bad sentences or parsing errors%n", getClass().getSimpleName(),
					parsingErrors);
		return data;
	}

	/**
	 * Recommended minimum specific GPS/Transit data
	 * (http://aprs.gids.nl/nmea/#rmc)
	 * 
	 * @param params
	 *            : Parameters extracted from GPRMC sentence
	 */
	private GPSData parseGPRMCSentence(String[] params) {
		GPSData gpsData = new GPSData();

		if (params.length == 13 || params.length == 10) {
			String[] d = params[9].split("(?<=\\G.{2})");

			params[1] = params[1].replace(".", "");
			String[] t = params[1].split("(?<=\\G.{2})");

			int miliseconds = Integer.parseInt(t[3] + t[4]);

			// LocalDateTime doesn't like milliseconds with
			// a value higher than 999, but NMEA does.
			while (miliseconds > 999)
				miliseconds /= 10;

			LocalDateTime date = new LocalDateTime(Integer.parseInt(d[2]) + 2000, Integer.parseInt(d[1]),
					Integer.parseInt(d[0]), Integer.parseInt(t[0]), Integer.parseInt(t[1]), Integer.parseInt(t[2]),
					miliseconds);
			gpsData.setDate(date);

			if (params[2].equals("V")) {
				gpsData.setFix(false);
			} else if (params[2].equals("A")) {
				gpsData.setFix(true);
			}

			if (!params[3].isEmpty() && !params[5].isEmpty()) {
				gpsData.setLatitude(params[3] + params[4]);
				gpsData.setLongitude(params[5] + params[6]);
			}

			gpsData.setGroundSpeedKnts(Double.parseDouble(params[7]));
			gpsData.setGroundSpeedKmh(Double.parseDouble(params[7]) * 1.85200);
			gpsData.setOrientation(Double.parseDouble(params[8]));

			return gpsData;
		} else
			throw new RuntimeException();
	}

	/**
	 * Gets the parsed GPS data
	 * 
	 * @return a map with the parsed GPS data where the robot index is the key
	 */
	public HashMap<Integer, List<GPSData>> getGPSData() {
		return gpsData;
	}

	public void saveParsedDataToFile() throws FileAlreadyExistsException, FileSystemException {
		File file = new File(INPUT_FOLDER, PARSED_DATA_FILE_GPS);
		if (file.exists()) {
			throw new FileAlreadyExistsException("File already exist");
		} else {
			FileUtils.ExperiencesDataOnFile data_gps = new FileUtils.ExperiencesDataOnFile();
			data_gps.setGPSData(gpsData);
			if (!FileUtils.saveDataToFile(data_gps, PARSED_DATA_FILE_GPS, true)) {
				throw new FileSystemException("Error writing GPS data to file");
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		System.out.printf("[%S] [INIT]%n", GPSLogFilesParser.class.getSimpleName());
		new GPSLogFilesParser(INPUT_FOLDER);
		System.out.printf("[%S] [FINISHED]%n", GPSLogFilesParser.class.getSimpleName());
	}
}
