#!/usr/bin/perl -w

open(FILE, "<allprot.txt") || die "Datei nicht gefunden";
open(NEW_FILE, ">bt426.list") || die "Datei nicht gefunden";
my @lines;

while (<FILE>)
{
	$string = $_;
	$string =~ s/(^\w{4})(\w{1})/$1.$2/gi;
	push(@lines,uc($string));
}
close(FILE);
print NEW_FILE @lines;
close(NEW_FILE);

