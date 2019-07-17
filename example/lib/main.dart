import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io';

import 'package:package_replaced/package_replaced.dart';
import 'package:path_provider/path_provider.dart' as path_provider;

void main(){
  runApp(MyApp());
  PackageReplaced.setHandlerFunction(handlePackageReplaced);
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

void handlePackageReplaced() async{
  await _incrementReplaceCount();
}

class _MyAppState extends State<MyApp> {
  int _replaceCount = 0;
  String _miscMessage = "";

  @override
  void initState() {
    super.initState();
    _showReplaceCount();
  }

  void _showReplaceCount() async{
    final replaceCount = await _getReplaceCount();

    setState((){
      _replaceCount = replaceCount;
    });
  }

  void _artificalIncrement() async{
    await _incrementReplaceCount();
    _showReplaceCount();
  }

  void _artificalReset() async{
    await _setReplaceCount(0);
    _showReplaceCount();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('package_replaced plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('Package replaced $_replaceCount time(s)'),
              Text(_miscMessage)
            ],
          ),
        ),
        floatingActionButton: Column(
          children: <Widget>[
            FloatingActionButton(
              child:Text("+"),
              onPressed: _artificalIncrement,
            ),
            FloatingActionButton(
              child:Text("0"),
              onPressed: _artificalReset,
            ),
          ],

          mainAxisAlignment: MainAxisAlignment.end,
        )
      ),
    );
  }
}

Future<File> getCountFile() async{
  final myStorageDirectory = await path_provider.getApplicationDocumentsDirectory();

  var countFilePath = myStorageDirectory.path;

  if(!countFilePath.endsWith("/")){
    countFilePath += "/";
  }

  countFilePath += "org.thebus.package_replaced_example.replaceCount";

  return File(countFilePath);
}

Future<int> _getReplaceCount() async{
  var retCount = 0;

  final countFile = await getCountFile();

  if(await countFile.exists()){
    retCount = int.tryParse((await countFile.readAsString()));

    if(retCount == null){
      retCount = 0;
    }
  }

  return retCount;
}

Future<void> _setReplaceCount(int newCountValue) async{
  final countFile = await getCountFile();

  if(!(await countFile.parent.exists())){
    await countFile.parent.create(recursive: true);
  }

  await countFile.writeAsString(newCountValue.toString());
}

Future<void> _incrementReplaceCount() async{
  final currentReplaceCount = await _getReplaceCount();
  await _setReplaceCount(currentReplaceCount+1);
}