<?php
/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
 /**
  * Small example showing how to use DL-Learner for reasoning. 
  * 
  * @author Jens Lehmann
  */
  
include('Utilities.php');

// load WSDL files (has to be done due to a Java web service bug)
ini_set("soap.wsdl_cache_enabled","0");
$wsdluri="http://localhost:8181/services?wsdl";
Utilities::loadWSDLfiles($wsdluri);

// specifiy ontology
$ontology = 'file:'.realpath("../../examples/family/father.owl");

// create DL-Learner client
$client = new SoapClient("main.wsdl");

// load owl file in DIG reasoner (you need a running DIG reasoner)
$id = $client->generateID();
$ksID = $client->addKnowledgeSource($id, "owlfile", $ontology);
$client->init($id, $ksID);
$rID = $client->setReasoner($id, "dig");
$client->init($id, $rID);

// create a concept in internal DL-Learner syntax
// ( = all female persons having at least one child)
$concept = '("http://example.com/father#female" AND EXISTS "http://example.com/father#hasChild".TOP)';
$instances = $client->retrieval($id, $concept);

// print instances    
echo 'instances of ' . $concept . ': <br />';                
echo '<pre>';                  
print_r($instances->item);
echo '</pre>';

?>