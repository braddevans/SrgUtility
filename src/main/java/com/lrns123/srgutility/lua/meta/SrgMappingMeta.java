package com.lrns123.srgutility.lua.meta;

import java.io.File;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.VarArgFunction;

import com.lrns123.srgutility.srg.SrgMapping;
import com.lrns123.srgutility.transformer.MappingTransformer;
import static com.lrns123.srgutility.lua.util.LuaUtil.getTransformerFromArg;

public class SrgMappingMeta extends LibFunction
{
	private static LuaTable metatable;

	public static LuaTable getMetaTable()
	{
		if (metatable == null)
			new SrgMappingMeta();
		return metatable;
	}

	private SrgMappingMeta()
	{
		metatable = new LuaTable();

		bind(metatable, SrgMappingMetaV.class, new String[] { "saveToFile", "clone", "reverse", "identity", "transform", "filter" });
		metatable.set(INDEX, metatable);
	}

	public static final class SrgMappingMetaV extends VarArgFunction
	{
		@Override
		public Varargs invoke(Varargs args)
		{
			switch (opcode)
			{
				case 0: // mapping:save(filename)
					return saveToFile(args.arg1(), args.arg(2));
				case 1: // mapping:clone()
					return SrgMappingMeta.clone(args.arg1());
				case 2: // mapping:reverse()
					return reverse(args.arg1());
				case 3: // mapping:identity()
					return identity(args.arg1());
				case 4: // mapping:identity(inputTransformer, outputTransformer)
					return transform(args.arg1(), args.arg(2), args.arg(3));
				case 5: // mapping:filter(filterSrg)
					return filter(args.arg1(), args.arg(2));
			}
			return LuaValue.NIL;
		}
	}

	private static LuaValue saveToFile(LuaValue instance, LuaValue fileArg)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);
		String filename = fileArg.checkjstring();

		try
		{
			mapping.write(new File(filename));
			return LuaValue.NIL;
		}
		catch (Exception e)
		{
			throw new LuaError(e);
		}
	}

	private static LuaValue clone(LuaValue instance)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);
		return new LuaUserdata(mapping.clone(), SrgMappingMeta.getMetaTable());
	}

	private static LuaValue reverse(LuaValue instance)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);
		return new LuaUserdata(mapping.reverse(), SrgMappingMeta.getMetaTable());
	}
	
	private static LuaValue identity(LuaValue instance)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);
		return new LuaUserdata(mapping.identity(), SrgMappingMeta.getMetaTable());
	}

	private static LuaValue transform(LuaValue instance, LuaValue inputTransformerArg, LuaValue outputTransformerArg)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);

		MappingTransformer inputTransformer = getTransformerFromArg(inputTransformerArg);
		MappingTransformer outputTransformer = getTransformerFromArg(outputTransformerArg);

		return new LuaUserdata(mapping.transform(inputTransformer, outputTransformer), SrgMappingMeta.getMetaTable());
	}
	
	private static LuaValue filter(LuaValue instance, LuaValue filter)
	{
		SrgMapping mapping = (SrgMapping) instance.checkuserdata(SrgMapping.class);
		SrgMapping filterMapping = (SrgMapping) filter.checkuserdata(SrgMapping.class);

		return new LuaUserdata(mapping.filter(filterMapping), SrgMappingMeta.getMetaTable());
	}
}