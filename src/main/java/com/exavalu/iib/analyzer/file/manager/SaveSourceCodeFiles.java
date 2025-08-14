package com.exavalu.iib.analyzer.file.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.utils.FileNameExtensionExtract;

public class SaveSourceCodeFiles {
	private static final Logger log = LoggerFactory.getLogger(SaveSourceCodeFiles.class);

	public static String[] saveProjectFilesToLocal(String fileName, String userID, String zipFileName, int flagMultiZip,
			ZipInputStream zipInputStream, String projectName) {

		final int BUFFER_SIZE = 4096;

		String fullPath = "", commonPath = "", variablePath = "", fileType = "", fileExtensions = "", subFolder = "";
		String[] paths = new String[2];

		fileExtensions = FileNameExtensionExtract.getFileExtension(fileName).toLowerCase();

		switch (fileExtensions) {
		case "project":
			subFolder = "project";
			break;
		case "msgflow":
			subFolder = "msgflow";
			break;
		case "subflow":
			subFolder = "subflow";
			break;
		case "esql":
			subFolder = "esql";
			break;
		case "json":
			subFolder = "json";
			break;
		case "descriptor":
			subFolder = "descriptor";
			break;
		case "wsdl":
			subFolder = "wsdl";
			break;
		case "xsd":
			subFolder = "xsd";
			break;
		case "cpy":
			subFolder = "cpy";
			break;
		case "yaml":
			subFolder = "yaml";
			break;
		case "properties":
			subFolder = "properties";
			break;
		default:
			subFolder = "otherFiles";
		}

		try {
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
			String uploadPath = DirectoryManager.getDirectory();

			// CREATION OF THE DIRECTORY WITH THE UPLOAD ID INTO THE USERNAME DIRECTORY.
			File mainDirectory = new File(uploadPath);
			if (!mainDirectory.exists()) {
				boolean isDIrectory = mainDirectory.mkdirs();
				if (AppGlobalDeclaration.isLogEnabled) {
					if (isDIrectory == true)
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Successfully created"
								+ mainDirectory.getAbsolutePath());
					else if (AppGlobalDeclaration.isErrorLogEnabled) {
						log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Unable to created");
					}
				}
			}

			// Folder of Folder Create
			int lengthStearm;
			byte[] buffer = new byte[BUFFER_SIZE];
			fileName = fileName.substring(fileName.indexOf("\\") + 1);

			File newFile;
			commonPath = uploadPath + System.getProperty("file.separator") + zipFileName;
			variablePath = System.getProperty("file.separator") + projectName + System.getProperty("file.separator")
					+ subFolder + System.getProperty("file.separator") + fileName;
			newFile = new File(commonPath + variablePath);
			fullPath = newFile.toString();

			// CREATION OF THE DIRECTORIES AND SUBDIRECTORIES INTO THE ZIP
			new File(newFile.getParent()).mkdirs();
			// FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(newFile);
				while ((lengthStearm = zipInputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, lengthStearm);
				}
			} catch (Exception e) {
			} finally {
				zipInputStream.closeEntry();
				if (fileOutputStream != null)
					fileOutputStream.close();
			}

			paths[0] = commonPath;
			paths[1] = variablePath;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paths;
	}
}
