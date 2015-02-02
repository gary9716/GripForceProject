How to use?
instead of using api Typeface.createFromAsset(AssetManager mgr, String path);
I would put my font file in the folder called fonts under system external storage(get the path via Environment.getExternalStorageDirectory().getAbsolutePath())
and use api Typeface.createFromFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/fonts/" + <font_file_name>);
