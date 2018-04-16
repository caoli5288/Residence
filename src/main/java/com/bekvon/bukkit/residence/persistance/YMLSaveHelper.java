package com.bekvon.bukkit.residence.persistance;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class YMLSaveHelper {

	private File f;
	private Yaml yml;
	private Map<String, Object> root;

	public YMLSaveHelper(File ymlfile) throws IOException {
		f = Objects.requireNonNull(ymlfile);
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setAllowUnicode(true);
		yml = new Yaml(options);
		root = new LinkedHashMap<>();
	}

	public void save() throws IOException {
		Files.write(f.toPath(), yml.dump(root).getBytes("utf8"));
		System.out.println("#YML_SAVER saved " + f + " content(s) size " + f.length());
	}

	@SuppressWarnings("unchecked")
	public void load() throws IOException {
		InputStream fis = new FileInputStream(f);
		try {
			root = yml.load(fis);
		} catch (ReaderException e) {
			System.out.println("[Residence] - Failed to load " + yml.getName() + " file!");
		}
		fis.close();
	}

	public Map<String, Object> getRoot() {
		return root;
	}
}
