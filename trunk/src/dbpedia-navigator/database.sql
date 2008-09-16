-- phpMyAdmin SQL Dump
-- version 2.9.1
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Erstellungszeit: 20. August 2008 um 12:05
-- Server Version: 5.0.67
-- PHP-Version: 5.2.5
-- 
-- Datenbank: `navigator_db`
-- 
CREATE DATABASE `navigator_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `navigator_db`;

-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `articlecategories`
-- 

CREATE TABLE `articlecategories` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(330) NOT NULL,
  `category` varchar(330) NOT NULL,
  `number` int(8) default '0',
  PRIMARY KEY  (`id`),
  KEY `Category` (`category`),
  KEY `Names` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- 
-- Daten für Tabelle `articlecategories`
-- 


-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `categories`
-- 

CREATE TABLE `categories` (
  `category` varchar(330) NOT NULL,
  `label` varchar(330) NOT NULL,
  PRIMARY KEY  (`category`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- 
-- Daten für Tabelle `categories`
-- 


-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `classhierarchy`
-- 

CREATE TABLE `classhierarchy` (
  `id` int(11) NOT NULL auto_increment,
  `father` varchar(330) NOT NULL,
  `child` varchar(330) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Father` (`father`),
  KEY `Child` (`child`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- 
-- Daten für Tabelle `classhierarchy`
-- 


-- --------------------------------------------------------

-- 
-- Tabellenstruktur für Tabelle `rank`
-- 

CREATE TABLE `rank` (
  `name` varchar(330) NOT NULL,
  `label` varchar(330) default NULL,
  `number` int(8) NOT NULL default '0',
  PRIMARY KEY  (`name`),
  FULLTEXT KEY `Label` (`label`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- 
-- Daten für Tabelle `rank`
-- 

