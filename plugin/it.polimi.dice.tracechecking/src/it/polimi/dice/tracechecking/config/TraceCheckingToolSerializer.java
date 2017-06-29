package it.polimi.dice.tracechecking.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class TraceCheckingToolSerializer {

		public static String serialize(List<TopologyNodeFormula> config) {
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			return gson.toJson(config);
		}

		public static List<TopologyNodeFormula> deserialize(String serializedConfig) throws IOException {
			Gson gson = new GsonBuilder().create();
			TopologyNodeFormula[] tnfArray;
			tnfArray = gson.fromJson(serializedConfig, TopologyNodeFormula[].class);
			return new ArrayList<>(Arrays.asList(tnfArray));
		}
		
	}
