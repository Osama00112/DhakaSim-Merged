# Run this script with input link and node files of your format eg. python input_cnvrt.py node_in.txt link_in.txt
# This script will generate the node.txt & link.txt file for DhakaSim
# It will also generate two files indicating the mapping of nodes and links
# You can look up these files for your debugging purpose

from sys import argv


def getLinkMap(input_link_file_name):
    link_map = {}
    link_in = open(input_link_file_name)
    no_of_links = int(link_in.readline())
    lid = 0

    for i in range(no_of_links):
        link_id, node1, node2, no_of_segments = map(int, link_in.readline().split(" "))
        if link_id not in link_map:
            link_map[link_id] = lid
            lid += 1
        for j in range(no_of_segments):
            next(link_in)
    link_in.close()
    return link_map

def getNodeMap(input_node_file_name):
    node_map = {}
    node_in = open(input_node_file_name)
    no_of_nodes = int(node_in.readline())
    nid = 0

    for i in range(no_of_nodes):
        line = node_in.readline().strip().split(" ")
        node_id = int(line[0])
        if node_id not in node_map:
            node_map[node_id] = nid
            nid += 1
    node_in.close()
    return node_map

def generateNodeOutputFile(node_map, link_map, input_node_file, output_node_file):
    node_in = open(input_node_file)
    node_out = open(output_node_file, "w")

    no_of_nodes = int(node_in.readline())
    print(no_of_nodes, file=node_out)
    for i in range(no_of_nodes):
        line = node_in.readline().strip().split()
        node_id = int(line[0])
        x, y = line[1], line[2]
        links = line[3:]
        outlinks = []
        outstring = [node_map[node_id], x, y]
        for link in links:
            if int(link) in link_map:
                outstring.append(link_map[int(link)])
            else:
                print("Link " + link + " is not found")
        for things in outstring:
            print(things, end=" ", file=node_out)
        print(file=node_out)

def generateLinkOutputFile(node_map, link_map, input_link_file, output_link_file):
    link_in = open(input_link_file)
    link_out = open(output_link_file, "w")
    no_of_links = int(link_in.readline())
    print(no_of_links, file=link_out)
    for i in range(no_of_links):
        link_id, start_node, end_node, no_of_segments = map(int, link_in.readline().strip().split())

        outstring = [link_map[link_id]]
        if start_node not in node_map:
            print("Node", start_node, "is not found")
        else:
            outstring.append(node_map[start_node])
        if end_node not in node_map:
            print("Node", end_node, "is not found")
        else:
            outstring.append(node_map[end_node])
        
        outstring.append(no_of_segments)
        if len(outstring) == 4:
            for things in outstring:
                print(things, end=" ", file=link_out)
            print(file=link_out)
            for j in range(no_of_segments):
                print(link_in.readline(), end="", file=link_out)
        else:
            # print("Problem", outstring)
            for j in range(no_of_segments):
                next(link_in)


def writeMapping(node_map, link_map):
    fnode_map = open("node_map.txt", "w")
    flink_map = open("link_map.txt", "w")
    for in_node, out_node in node_map.items():
        print(in_node, " -> ", out_node, file=fnode_map)
        
    for in_link, out_link in link_map.items():
        print(in_link, " -> ", out_link, file=flink_map)
    
if __name__ == "__main__":
    script, node_input_file, link_input_file = argv

    node_map = getNodeMap(node_input_file)
    link_map = getLinkMap(link_input_file)

    writeMapping(node_map, link_map)

    generateNodeOutputFile(node_map, link_map, node_input_file, "node.txt")
    generateLinkOutputFile(node_map, link_map, link_input_file, "link.txt")
