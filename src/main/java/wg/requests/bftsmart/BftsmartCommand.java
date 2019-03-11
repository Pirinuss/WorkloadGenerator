package wg.requests.bftsmart;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.workload.parser.WorkloadParserException;

public class BftsmartCommand {

	private static final Logger log = LoggerFactory
			.getLogger(BftsmartCommand.class);

	private final BftsmartCommandType type;
	private final String content;
	private final ByteArrayOutputStream byteOut;

	public BftsmartCommand(JSONObject command) throws WorkloadParserException {

		// Type
		String type = (String) command.get("type");
		if (type == null) {
			throw new IllegalArgumentException(
					"Command type must not be null!");
		}
		BftsmartCommandType commandType = BftsmartCommandType.fromString(type);
		this.type = commandType;

		// Content
		String content = (String) command.get("content");
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		// ByteArrayOutputStream
		this.byteOut = createByteOut(command);

	}

	public ByteArrayOutputStream getByteOut() {
		return byteOut;
	}

	private ByteArrayOutputStream createByteOut(JSONObject command)
			throws WorkloadParserException {
		if (type == BftsmartCommandType.OBJECT_OUTPUT_STREAM) {
			return createByteOutObjectStream(command);
		} else {
			return createByteOutDataStream();
		}

	}

	private ByteArrayOutputStream createByteOutDataStream()
			throws WorkloadParserException {

		byte[] contentBytes = Base64.decodeBase64(content);

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

		try {
			new DataOutputStream(byteOut).write(contentBytes);
		} catch (IOException e) {
			throw new WorkloadParserException(
					"Error while creating data output!", e);
		}

		return byteOut;
	}

	private ByteArrayOutputStream createByteOutObjectStream(JSONObject command)
			throws WorkloadParserException {

		ByteArrayOutputStream byteOut = null;

		String[] params = content.split(",");
		if (params.length == 0) {
			throw new IllegalArgumentException("Invalid content!");
		}
		try {
			byteOut = new ByteArrayOutputStream();
			ObjectOutput objOut = new ObjectOutputStream(byteOut);
			for (int i = 0; i < params.length; i++) {
				String[] param = params[i].split(":");
				if (param.length != 2) {
					throw new IllegalArgumentException("Invalid content!");
				}
				String id = param[0];
				String identifier = param[1];
				if (command.get(id) == null) {
					throw new IllegalArgumentException(
							"Content parameter not found! " + id);
				}
				objOut.writeObject(getObject(identifier, id, command));
			}
			objOut.flush();
			byteOut.flush();
		} catch (IOException e) {
			throw new WorkloadParserException(
					"Error while creating object output!", e);
		}
		return byteOut;
	}

	private static Object getObject(String identifier, String id,
			JSONObject command) throws WorkloadParserException {
		Object object = null;
		String stringParam = command.get(id).toString();

		switch (identifier.toUpperCase()) {
		case "BOOLEAN":
			if (!stringParam.toUpperCase().equals("TRUE")
					&& !stringParam.toUpperCase().equals("FALSE")) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Boolean.valueOf(stringParam);
			break;
		case "CHAR":
			if (stringParam.length() != 1) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Character.valueOf(stringParam.charAt(0));
			break;
		case "BYTE":
			object = Byte.valueOf(stringParam);
			break;
		case "SHORT":
			object = Short.valueOf(stringParam);
			break;
		case "INT":
			object = Integer.valueOf(stringParam);
			break;
		case "LONG":
			object = Long.valueOf(stringParam);
			break;
		case "FLOAT":
			object = Float.valueOf(stringParam);
			break;
		case "DOUBLE":
			object = Double.valueOf(stringParam);
			break;
		case "STRING":
			object = String.valueOf(stringParam);
			break;
		case "ENUMOBJECT":
			object = getExternObject(command.get(id), true);
			break;
		case "OBJECT":
			object = getExternObject(command.get(id), false);
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier" + identifier);
		}
		return object;
	}

	private static Object getExternObject(Object objectDes, boolean isEnum)
			throws WorkloadParserException {

		if (objectDes == null
				|| !objectDes.getClass().equals(JSONObject.class)) {
			throw new IllegalArgumentException(
					"Invalid description of enum object!");
		}

		JSONObject descriptor = (JSONObject) objectDes;

		String path = (String) descriptor.get("path");
		String classname = (String) descriptor.get("classname");

		if (path == null || classname == null) {
			throw new IllegalArgumentException(
					"Invalid description of extern object!");
		}

		try {
			File file = new File(path);
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			final URLClassLoader cl = new URLClassLoader(urls);
			Class<?> objectClass = cl.loadClass(classname);
			cl.close();

			if (isEnum) {
				String type = (String) descriptor.get("type");
				return getEnumObject(objectClass, type);
			} else {
				String constructor = (String) descriptor.get("constructor");
				return getClassObject(objectClass, constructor, descriptor);
			}

		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed path! " + path, e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found! " + classname,
					e);
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (SecurityException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	private static Object getEnumObject(Class<?> objectClass, String type) {

		if (type == null) {
			throw new IllegalArgumentException(
					"No enum type found in JSON document!");
		}

		Object[] enums = objectClass.getEnumConstants();
		for (int i = 0; i < enums.length; i++) {
			if (enums[i].toString().toUpperCase().equals(type.toUpperCase())) {
				return enums[i];
			}
		}
		throw new IllegalArgumentException("Enum type not found: " + type);
	}

	private static Object getClassObject(Class<?> objectClass,
			String constructor, JSONObject descriptor)
			throws IOException, WorkloadParserException {

		try {
			Constructor<?> c;
			Object[] initArgs = null;

			if (constructor == null) {
				c = objectClass.getConstructor();
			} else {
				String[] params = constructor.split(",");
				Class<?>[] classes = new Class[params.length];
				initArgs = new Object[params.length];

				for (int i = 0; i < params.length; i++) {
					String[] param = params[i].split(":");
					if (param.length != 2) {
						throw new IllegalArgumentException(
								"Invalid constructor!");
					}
					String id = param[0];
					String identifier = param[1];
					if (descriptor.get(id) == null) {
						throw new IllegalArgumentException(
								"Constructor parameter not found! " + id);
					}
					Object object = getObject(identifier, id, descriptor);
					initArgs[i] = object;
					classes[i] = object.getClass();
				}

				c = objectClass.getConstructor(classes);
			}

			if (initArgs == null) {
				return c.newInstance();
			} else {
				return c.newInstance(initArgs);
			}
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			log.error(e.getMessage());
			throw new WorkloadParserException(
					"Error while loading extern Object", e);
		}
	}

}
