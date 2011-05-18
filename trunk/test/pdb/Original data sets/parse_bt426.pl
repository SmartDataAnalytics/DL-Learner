#!/usr/bin/perl -w

open(FILE, "<bt426.list") || die "Datei nicht gefunden";
open(NEW_FILE, ">bt426.list.new") || die "Datei nicht gefunden";
my @lines;

while (<FILE>)
{
	$string = $_;
	$string =~ s/(^\w{4}\.)\n/$1\.\n/gi;
	push(@lines,uc($string));
}
close(FILE);
print NEW_FILE @lines;
close(NEW_FILE);

