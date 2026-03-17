import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateServiceDto, UpdateServiceDto } from './dto';

@Injectable()
export class ServicesService {
    constructor(private prisma: PrismaService) { }

    async create(shopId: string, userId: string, dto: CreateServiceDto) {
        // Verify shop exists and user owns it
        const shop = await this.prisma.shop.findUnique({ where: { id: shopId } });
        if (!shop) throw new NotFoundException('Shop not found');
        if (shop.ownerId !== userId) throw new ForbiddenException('You do not own this shop');

        return this.prisma.service.create({
            data: {
                ...dto,
                shopId,
            },
        });
    }

    async findAll(shopId: string) {
        return this.prisma.service.findMany({
            where: { shopId },
        });
    }

    async findOne(id: string) {
        const service = await this.prisma.service.findUnique({
            where: { id },
            include: { shop: { select: { name: true, ownerId: true } } },
        });
        if (!service) throw new NotFoundException('Service not found');
        return service;
    }

    async update(serviceId: string, userId: string, dto: UpdateServiceDto) {
        const service = await this.prisma.service.findUnique({
            where: { id: serviceId },
            include: { shop: true },
        });
        if (!service) throw new NotFoundException('Service not found');
        if (service.shop.ownerId !== userId) throw new ForbiddenException('Not authorized');

        return this.prisma.service.update({
            where: { id: serviceId },
            data: dto,
        });
    }

    async delete(serviceId: string, userId: string) {
        const service = await this.prisma.service.findUnique({
            where: { id: serviceId },
            include: { shop: true },
        });
        if (!service) throw new NotFoundException('Service not found');
        if (service.shop.ownerId !== userId) throw new ForbiddenException('Not authorized');

        return this.prisma.service.delete({ where: { id: serviceId } });
    }
}
