import xml.etree.ElementTree as ET

def generate_dtd(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()

    dtd_output = []

    def process_element(element, indent=""):
        dtd_line = f"{indent}<!ELEMENT {element.tag} ({','.join([child.tag for child in element])})>"
        dtd_output.append(dtd_line)
        
        if len(element.attrib) > 0:
            attr_lines = [f"{indent}<!ATTLIST {element.tag}"]
            for attr, value in element.attrib.items():
                attr_lines.append(f"{indent}    {attr} CDATA #IMPLIED")
            dtd_output.extend(attr_lines)

        for child in element:
            process_element(child, indent + "  ")

    process_element(root)

    return "\n".join(dtd_output)

# Usage example
xml_file = "example.xml"
generated_dtd = generate_dtd(xml_file)

# Write the generated DTD to a file
with open("generated.dtd", "w") as dtd_file:
    dtd_file.write(generated_dtd)
