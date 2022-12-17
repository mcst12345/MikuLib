package miku.lib.config;

//Copied from LoliPickaxe

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.common.config.Configuration;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MikuConfig {
    private static Configuration config;
    public static final List<String> flags = Lists.newArrayList();
    public static final List<String> commandFlags = Lists.newArrayList();
    public static final List<String> guiFlags = Lists.newArrayList();
    public static final Map<String, ConfigField> flagAnnotations = Maps.newHashMap();
    public static final Map<String, Field> flagFields = Maps.newHashMap();

    @ConfigField(
            type = {ConfigField.ConfigType.CONFIG},
            comment = "",
            valueType = ConfigField.ValurType.BOOLEAN
    )
    public static boolean UseMikuInsteadOfDevItemIfPossible;

    @ConfigField(
            type = {ConfigField.ConfigType.CONFIG},
            comment = "",
            valueType = ConfigField.ValurType.BOOLEAN,
            booleanDefaultValue = true
    )
    public static boolean AutoRangeKill;

    static {
        try {
            Field[] fields = MikuConfig.class.getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ConfigField.class)) {
                    ConfigField annotation = field.getAnnotation(ConfigField.class);
                    flags.add(field.getName());
                    flagAnnotations.put(field.getName(), annotation);
                    flagFields.put(field.getName(), field);
                    ConfigField.ConfigType[] types = annotation.type();
                    for (ConfigField.ConfigType type : types) {
                        switch (type) {
                            case COMMAND:
                                commandFlags.add(field.getName());
                                break;
                            case GUI:
                                guiFlags.add(field.getName());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        config = new Configuration(new File("config/mikulib.cfg"));
        load(false);
    }

    public static void load(boolean reload) {
        config.load();
        try {
            for (String flag : flags) {
                Field field = flagFields.get(flag);
                ConfigField annotation = flagAnnotations.get(flag);
                if (reload) {
                    boolean canReload = false;
                    for (ConfigField.ConfigType type : annotation.type()) {
                        if (type == ConfigField.ConfigType.CONFIG) {
                            canReload = true;
                            break;
                        }
                    }
                    if (!canReload) {
                        continue;
                    }
                }
                switch (annotation.valueType()) {
                    case INT:
                        field.setInt(null, config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.intDefaultValue(), annotation.comment()).getInt());
                        break;
                    case DOUBLE:
                        field.setDouble(null, config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.doubleDefaultValue(), annotation.comment()).getDouble());
                        break;
                    case BOOLEAN:
                        field.setBoolean(null, config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.booleanDefaultValue(), annotation.comment()).getBoolean());
                        break;
                    case STRING:
                        field.set(null, config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.stringDefaultValue(), annotation.comment()).getString());
                        break;
                    case LIST: {
                        String[] strs = config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.listDefaultValue(), annotation.comment()).getStringList();
                        List<@Nullable Object> list = Lists.newArrayList();
                        for (String str : strs) {
                            switch (annotation.listType()) {
                                case INT:
                                    list.add(Integer.parseInt(str));
                                    break;
                                case DOUBLE:
                                    list.add(Double.parseDouble(str));
                                    break;
                                case BOOLEAN:
                                    list.add(Boolean.parseBoolean(str));
                                    break;
                                case STRING:
                                    list.add(str);
                                    break;
                                default:
                            }
                        }
                        field.set(null, list);
                        break;
                    }
                    case MAP: {
                        String[] strs = config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.mapDefaultValue(), annotation.comment()).getStringList();
                        Map<@Nullable Object, @Nullable Object> map = Maps.newHashMap();
                        for (String str : strs) {
                            int index = str.lastIndexOf(":::");
                            Object key;
                            switch (annotation.mapKeyType()) {
                                case INT:
                                    key = Integer.parseInt(str.substring(0, index));
                                    break;
                                case DOUBLE:
                                    key = Double.parseDouble(str.substring(0, index));
                                    break;
                                case BOOLEAN:
                                    key = Boolean.parseBoolean(str.substring(0, index));
                                    break;
                                case STRING:
                                    key = str.substring(0, index);
                                    break;
                                default:
                                    continue;
                            }
                            Object value;
                            switch (annotation.mapValueType()) {
                                case INT:
                                    value = Integer.parseInt(str.substring(index + 3));
                                    break;
                                case DOUBLE:
                                    value = Double.parseDouble(str.substring(index + 3));
                                    break;
                                case BOOLEAN:
                                    value = Boolean.parseBoolean(str.substring(index + 3));
                                    break;
                                case STRING:
                                    value = str.substring(index + 3);
                                    break;
                                default:
                                    continue;
                            }
                            map.put(key, value);
                        }
                        field.set(null, map);
                        break;
                    }
                    default:
                        break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        config.save();
    }

    public static void save() {
        try {
            for (String flag : flags) {
                Field field = flagFields.get(flag);
                ConfigField annotation = flagAnnotations.get(flag);
                boolean canSave = false;
                for (ConfigField.ConfigType type : annotation.type()) {
                    if (type == ConfigField.ConfigType.CONFIG) {
                        canSave = true;
                        break;
                    }
                }
                if (!canSave) {
                    continue;
                }
                switch (annotation.valueType()) {
                    case INT:
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.intDefaultValue(), annotation.comment()).setValue(field.getInt(null));
                        break;
                    case DOUBLE:
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.doubleDefaultValue(), annotation.comment()).setValue(field.getDouble(null));
                        break;
                    case BOOLEAN:
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.booleanDefaultValue(), annotation.comment()).setValue(field.getBoolean(null));
                        break;
                    case STRING:
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.stringDefaultValue(), annotation.comment()).setValue((String) field.get(null));
                        break;
                    case LIST: {
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.listDefaultValue(), annotation.comment()).setValues(((List<?>) field.get(null)).stream().map(Object::toString).toArray(String[]::new));
                        break;
                    }
                    case MAP: {
                        config.get(Configuration.CATEGORY_GENERAL, field.getName(), annotation.mapDefaultValue(), annotation.comment()).setValues(((Map<?, ?>) field.get(null)).entrySet().stream().map(entry -> entry.getKey().toString() + ":::" + entry.getValue().toString()).toArray(String[]::new));
                        break;
                    }
                    default:
                        break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        config.save();
    }
}
