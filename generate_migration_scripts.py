import os

libs_dir = 'lib'
install_script = 'install-libs.sh'
pom_file = 'pom.xml'
group_id = 'local'
version = '1.0'

# Manual mapping for some jars if needed, otherwise use filename
# We will use filename as artifactId and ignore version in filename for simplicity
# or try to extract it.

def get_artifact_info(filename):
    name = filename
    if filename.endswith('.jar'):
        name = filename[:-4]

    # Try to split version
    # heuristic: split by - and see if part starts with digit
    parts = name.split('-')
    artifact_id = name
    ver = '1.0'

    for i, part in enumerate(parts):
        if part[0].isdigit():
            artifact_id = "-".join(parts[:i])
            ver = "-".join(parts[i:])
            break

    if artifact_id == '': # e.g. 1.0.jar
        artifact_id = name
        ver = '1.0'

    return artifact_id, ver

with open(install_script, 'w') as f:
    f.write('#!/bin/bash\n')
    f.write('mkdir -p ~/.m2/repository\n')

    dependencies = []

    for filename in sorted(os.listdir(libs_dir)):
        if filename.endswith('.jar') and os.path.isfile(os.path.join(libs_dir, filename)):
            artifact_id, ver = get_artifact_info(filename)

            # Special cases or corrections
            if filename == 'javax.servlet.jar':
                artifact_id = 'servlet-api'
                ver = '2.3' # Assuming old servlet api based on context

            # Using 'local' group id to avoid conflicts and ensure we use the provided libs
            cmd = f'mvn install:install-file -Dfile="{libs_dir}/{filename}" -DgroupId={group_id} -DartifactId={artifact_id} -Dversion={ver} -Dpackaging=jar'
            f.write(cmd + '\n')

            dependencies.append((group_id, artifact_id, ver))

print(f"Generated {install_script}")

# Generate dependencies XML block
print("<dependencies>")
for g, a, v in dependencies:
    print(f"    <dependency>")
    print(f"        <groupId>{g}</groupId>")
    print(f"        <artifactId>{a}</artifactId>")
    print(f"        <version>{v}</version>")
    print(f"    </dependency>")
print("</dependencies>")
