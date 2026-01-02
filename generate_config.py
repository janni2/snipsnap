import os
import datetime

def get_camel_case(name, prefix):
    if name.startswith(prefix):
        name = name[len(prefix):]

    parts = name.split('.')
    result = []
    for part in parts:
        if part:
            result.append(part[0].upper() + part[1:])
    return "".join(result)

def parse_conf(conf_file, prefix="app."):
    constants = []

    # Read properties
    props = {}
    with open(conf_file, 'r') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue

            if '=' in line:
                key, value = line.split('=', 1)
                key = key.strip()
                props[key] = value # We actually just need keys, value is not used for constant generation

    # Sort keys to match TreeSet behavior
    sorted_keys = sorted(props.keys())

    constants.append("")
    constants.append(f"  // automatically created interface/constants stub from")
    constants.append(f"  // {conf_file}")
    constants.append(f"  // generated on {datetime.datetime.now().strftime('%d.%m.%y %H:%M')}")

    for property_name in sorted_keys:
        # Create constant
        # public final static String APP_ADMIN_EMAIL = "app.admin.email";
        const_name = property_name.upper().replace('.', '_')
        constants.append(f"  // constant/getter for '{property_name}'")
        constants.append(f"  public final static String {const_name} = \"{property_name}\";")

        # Create getter/setter
        # public String getAdminEmail();
        # public String setAdminEmail(String value);

        camel_name = get_camel_case(property_name, prefix)
        constants.append(f"  public String get{camel_name}();")
        constants.append(f"  public String set{camel_name}(String value);")

    return constants

def generate_file(tmpl_file, conf_file, output_file, prefix="app."):
    constants = parse_conf(conf_file, prefix)
    defaults_block = "\n".join(constants)

    with open(tmpl_file, 'r') as f:
        content = f.read()

    content = content.replace('@DEFAULTS@', defaults_block)

    # Ensure directory exists
    os.makedirs(os.path.dirname(output_file), exist_ok=True)

    with open(output_file, 'w') as f:
        f.write(content)

    print(f"Generated {output_file}")

# Generate Globals.java
generate_file(
    'src/org/snipsnap/config/Globals.java.tmpl',
    'src/org/snipsnap/config/globals.conf',
    'src/org/snipsnap/config/Globals.java',
    prefix="app."
)

# Generate Configuration.java
generate_file(
    'src/org/snipsnap/config/Configuration.java.tmpl',
    'src/org/snipsnap/config/defaults.conf',
    'src/snipsnap/api/config/Configuration.java',
    prefix="app."
)
