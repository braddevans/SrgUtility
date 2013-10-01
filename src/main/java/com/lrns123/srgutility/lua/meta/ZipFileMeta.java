package com.lrns123.srgutility.lua.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ZipFileMeta extends LibFunction
{
	private static LuaTable metatable;

	public static LuaTable getMetaTable()
	{
		if (metatable == null)
			new ZipFileMeta();
		return metatable;
	}

	private ZipFileMeta()
	{
		metatable = new LuaTable();

		bind(metatable, ZipFileMetaV.class, new String[] { "close", "entries", "getEntry", "getName", "size", "readAll", "extract", "__gc" });
		metatable.set(INDEX, metatable);
	}

	public static final class ZipFileMetaV extends VarArgFunction
	{
		@Override
		public Varargs invoke(Varargs args)
		{
			switch (opcode)
			{
				case 0: // zipfile:close()
					return close(args.arg1());
				case 1: // zipfile:entries()
					return entries(args.arg1());
				case 2: // zipfile:getEntry(path)
					return getEntry(args.arg1(), args.arg(2));
				case 3: // zipfile:getName()
					return getName(args.arg1());
				case 4: // zipfile:size()
					return size(args.arg1());
				case 5: // zipfile:readAll(entry)
					return readAll(args.arg1(), args.arg(2));
				case 6: // zipfile:extract(entry, destFile)
					return extract(args.arg1(), args.arg(2), args.arg(3));
				case 7: // __gc
					return close(args.arg1());
			}
			return LuaValue.NIL;
		}
	}

	private static LuaValue close(LuaValue instance)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		
		try
		{
			zipFile.close();
			return LuaValue.NIL;
		}
		catch (Exception e)
		{
			throw new LuaError(e);
		}
	}

	private static LuaValue entries(LuaValue instance)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		
		Enumeration<? extends ZipEntry> files = zipFile.entries();
		LuaTable fileTable = new LuaTable();		
		int i = 1;
		while (files.hasMoreElements())
		{
			ZipEntry entry = files.nextElement();
			
			fileTable.set(i++, new LuaUserdata(entry)); // TODO: Metatable
		}
		
		return fileTable;
	}

	private static LuaValue getEntry(LuaValue instance, LuaValue pathArg)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		
		ZipEntry entry = zipFile.getEntry(pathArg.checkjstring());
		
		if (entry == null)
			return LuaValue.NIL;
		else
			return new LuaUserdata(entry);
	}
	
	private static LuaValue getName(LuaValue instance)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		return LuaValue.valueOf(zipFile.getName());
	}

	private static LuaValue size(LuaValue instance)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		return LuaValue.valueOf(zipFile.size());
	}
	
	private static LuaValue readAll(LuaValue instance, LuaValue entryArg)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		ZipEntry entry = (ZipEntry) entryArg.checkuserdata(ZipEntry.class);
		
		try
		{
			return LuaValue.valueOf(IOUtils.toString(zipFile.getInputStream(entry)));
		}
		catch (Exception e)
		{
			throw new LuaError(e);
		}
	}
	
	private static LuaValue extract(LuaValue instance, LuaValue entryArg, LuaValue pathArg)
	{
		ZipFile zipFile = (ZipFile) instance.checkuserdata(ZipFile.class);
		ZipEntry entry = (ZipEntry) entryArg.checkuserdata(ZipEntry.class);
		File destFile = new File(pathArg.checkjstring());
		
		try
		{
			InputStream inStream = zipFile.getInputStream(entry);
			FileOutputStream outStream = new FileOutputStream(destFile);
			
			
			final byte buffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer, 0, buffer.length)) != -1)
			{
				outStream.write(buffer, 0, bytesRead);
			}
			
			inStream.close();
			outStream.close();
			
			return LuaValue.NIL;
		}
		catch (Exception e)
		{
			throw new LuaError(e);
		}
	}
	
}
