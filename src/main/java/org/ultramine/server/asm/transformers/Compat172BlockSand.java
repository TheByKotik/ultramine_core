package org.ultramine.server.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.IClassTransformer;

public class Compat172BlockSand implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(name.startsWith("ttftcuts.atg."))
		{
			ClassReader reader = new ClassReader(basicClass);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

			ClassVisitor visitor = new ChangeDescVisitor(writer);

			reader.accept(visitor, 0);
			return writer.toByteArray();
		}
		
		return basicClass;
	}
	
	public static class ChangeDescVisitor extends ClassVisitor
	{
		private ChangeDescVisitor(ClassVisitor cv)
		{
			super(Opcodes.ASM4, cv);
		}
		
		@Override
		public MethodVisitor visitMethod(int mAccess, final String mName, final String mDesc, String mSignature, String[] mExceptions)
		{
			return new MethodVisitor(Opcodes.ASM4, super.visitMethod(mAccess, mName, mDesc, mSignature, mExceptions))
			{
				public void visitFieldInsn(int opcode, String owner, String name, String desc)
				{
					if(opcode == Opcodes.GETSTATIC && owner.equals("net/minecraft/init/Blocks") && name.equals("field_150354_m") && desc.equals("Lnet/minecraft/block/Block;"))
						desc = "Lnet/minecraft/block/BlockSand;";
					super.visitFieldInsn(opcode, owner, name, desc);
				}
			};
		}
	}
}
