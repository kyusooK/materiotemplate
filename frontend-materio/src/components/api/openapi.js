import orderYaml from "./order-openapi.yaml";
import inventoryYaml from "./inventory-openapi.yaml";
import YAML from "json2yaml";

let apiSpec = "";

let orderSpec = YAML.stringify(orderYaml);
apiSpec += orderSpec;

let inventorySpec = YAML.stringify(inventoryYaml);
apiSpec += inventorySpec;


export default apiSpec;