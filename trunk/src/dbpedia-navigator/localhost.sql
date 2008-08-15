-- phpMyAdmin SQL Dump
-- version 2.9.1
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Erstellungszeit: 15. August 2008 um 15:51
-- Server Version: 5.0.67
-- PHP-Version: 5.2.5
-- 
-- Datenbank: `navigator_db`
-- 
CREATE DATABASE `navigator_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `navigator_db`;

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
-- Tabellenstruktur für Tabelle `rank`
-- 

CREATE TABLE `rank` (
  `name` varchar(330) NOT NULL,
  `number` int(8) NOT NULL default '0',
  `label` varchar(330) default NULL,
  `category` varchar(330) default NULL,
  PRIMARY KEY  (`name`),
  FULLTEXT KEY `Label` (`label`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- 
-- Daten für Tabelle `rank`
-- 

